package com.bopin.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AdminPlatformService {
  private final JdbcTemplate jdbc;
  private final ObjectMapper mapper;

  public AdminPlatformService(JdbcTemplate jdbc, ObjectMapper mapper) {
    this.jdbc = jdbc;
    this.mapper = mapper;
  }

  private long now() { return Instant.now().toEpochMilli(); }
  private String id(String prefix) { return prefix + '_' + UUID.randomUUID().toString().replace("-", "").substring(0, 12); }
  private String json(Object value) {
    try { return mapper.writeValueAsString(value); } catch (Exception error) { throw new BusinessException("数据编码失败"); }
  }
  private List<String> strings(Object value) {
    if (value == null) return List.of();
    if (value instanceof List<?> list) return list.stream().map(String::valueOf).toList();
    try { return mapper.readValue(String.valueOf(value), new TypeReference<List<String>>() {}); } catch (Exception error) { return List.of(String.valueOf(value)); }
  }
  private Map<String, Object> object(Object value) {
    if (value == null) return new LinkedHashMap<>();
    if (value instanceof Map<?, ?> map) {
      Map<String, Object> result = new LinkedHashMap<>();
      map.forEach((key, item) -> result.put(String.valueOf(key), item));
      return result;
    }
    try { return mapper.readValue(String.valueOf(value), new TypeReference<Map<String, Object>>() {}); } catch (Exception error) { return new LinkedHashMap<>(); }
  }
  private String userId(String token) {
    if (token == null || token.isBlank()) throw new BusinessException("请先登录对应身份");
    var rows = jdbc.queryForList("SELECT user_id FROM auth_session WHERE token=? AND expires_at>?", token, now());
    if (rows.isEmpty()) throw new BusinessException("登录已失效，请重新登录");
    return String.valueOf(rows.get(0).get("USER_ID"));
  }
  private Map<String, Object> user(String token) { return one("SELECT * FROM app_user WHERE id=?", userId(token)); }
  private void requireRole(String token, String role) {
    if (!role.equalsIgnoreCase(text(user(token), "ROLE"))) throw new BusinessException(role.equals("merchant") ? "请切换到企业身份" : "请切换到主播身份");
  }
  private Map<String, Object> one(String sql, Object... args) {
    var rows = jdbc.queryForList(sql, args);
    return rows.isEmpty() ? null : rows.get(0);
  }
  private String text(Map<String, Object> row, String key) { return row == null || row.get(key) == null ? "" : String.valueOf(row.get(key)); }
  private int number(Map<String, Object> row, String key) { return row == null || row.get(key) == null ? 0 : ((Number) row.get(key)).intValue(); }
  private long longValue(Object value) {
    if (value instanceof Number number) return number.longValue();
    try { return Long.parseLong(String.valueOf(value)); } catch (Exception error) { return 0L; }
  }

  @Transactional
  public Map<String, Object> register(Map<String, Object> input) {
    String phone = input.get("phone") == null ? "" : String.valueOf(input.get("phone")).trim();
    if (phone.isBlank()) throw new BusinessException("手机号不能为空");
    String uid = id("u");
    String role = String.valueOf(input.getOrDefault("role", "anchor"));
    if (!role.equals("anchor") && !role.equals("merchant")) throw new BusinessException("身份类型不正确");
    String nickname = String.valueOf(input.getOrDefault("nickname", "新用户"));
    jdbc.update("INSERT INTO app_user(id,role,nickname,avatar,phone,verified,city,categories,intro,experience_years,card_status,card_data,created_at) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)", uid, role, nickname, "", phone, false, "", "[]", "", 0, "INCOMPLETE", "{}", now());
    jdbc.update("INSERT INTO user_wallet(user_id,card_balance,member_level,ai_quota,updated_at) VALUES(?,?,?,?,?)", uid, 3, "FREE", 3, now());
    jdbc.update("INSERT INTO message_quota(user_id,remaining_count,total_count) VALUES(?,?,?)", uid, 3, 3);
    String token = UUID.randomUUID().toString().replace("-", "");
    jdbc.update("INSERT INTO auth_session(token,user_id,expires_at) VALUES(?,?,?)", token, uid, now() + 30L * 24 * 3600 * 1000);
    return Map.of("token", token, "role", role, "user", profile(uid));
  }

  @Transactional
  public Map<String, Object> login(Map<String, Object> input) {
    String role = String.valueOf(input.getOrDefault("role", "anchor")).trim().toLowerCase();
    if (!role.equals("anchor") && !role.equals("merchant")) throw new BusinessException("身份类型不正确");
    boolean demo = Boolean.TRUE.equals(input.get("demo"));
    String defaultPhone = role.equals("merchant") ? "139****5200" : "138****6608";
    String phone = input.get("phone") == null || String.valueOf(input.get("phone")).isBlank() ? defaultPhone : String.valueOf(input.get("phone")).trim();
    var row = one("SELECT id FROM app_user WHERE role=? AND phone=?", role, phone);
    String uid;
    if (row == null) {
      if (demo) throw new BusinessException("演示账号不存在");
      uid = id("u");
      String nickname = role.equals("merchant") ? "新企业招聘者" : "新主播";
      jdbc.update("INSERT INTO app_user(id,role,nickname,avatar,phone,verified,city,categories,intro,experience_years,card_status,card_data,created_at) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)", uid, role, nickname, "", phone, false, "", "[]", "", 0, "INCOMPLETE", "{}", now());
      jdbc.update("INSERT INTO user_wallet(user_id,card_balance,member_level,ai_quota,updated_at) VALUES(?,?,?,?,?)", uid, role.equals("merchant") ? 0 : 3, role.equals("merchant") ? "BUSINESS" : "FREE", role.equals("merchant") ? 0 : 3, now());
      jdbc.update("INSERT INTO message_quota(user_id,remaining_count,total_count) VALUES(?,?,?)", uid, role.equals("merchant") ? 0 : 3, role.equals("merchant") ? 0 : 3);
    } else {
      uid = text(row, "ID");
    }
    String token = UUID.randomUUID().toString().replace("-", "");
    jdbc.update("INSERT INTO auth_session(token,user_id,expires_at) VALUES(?,?,?)", token, uid, now() + 30L * 24 * 3600 * 1000);
    return Map.of("token", token, "role", role, "user", profile(uid));
  }

  public Map<String, Object> profile(String uid) {
    var row = one("SELECT * FROM app_user WHERE id=?", uid);
    if (row == null) throw new BusinessException("用户不存在");
    boolean anchor = "anchor".equalsIgnoreCase(text(row, "ROLE"));
    if (anchor) migrateLegacyCard(row);
    List<Map<String, Object>> cards = anchor ? anchorCards(uid) : List.of();
    Map<String, Object> primaryCard = cards.stream().filter(card -> booleanValue(card.get("isPrimary"))).findFirst().orElse(cards.isEmpty() ? null : cards.get(0));
    Map<String, Object> resume = new LinkedHashMap<>();
    resume.put("nickname", text(row, "NICKNAME")); resume.put("categories", strings(row.get("CATEGORIES"))); resume.put("city", text(row, "CITY")); resume.put("intro", text(row, "INTRO")); resume.put("experienceYears", number(row, "EXPERIENCE_YEARS"));
    boolean hasResume = !text(row, "INTRO").isBlank() || !strings(row.get("CATEGORIES")).isEmpty() || !text(row, "CITY").isBlank() || number(row, "EXPERIENCE_YEARS") > 0;
    Map<String, Object> result = new LinkedHashMap<>();
    boolean cardCompleted = "merchant".equalsIgnoreCase(text(row, "ROLE")) || !cards.isEmpty();
    result.put("id", text(row, "ID")); result.put("role", text(row, "ROLE")); result.put("nickname", text(row, "NICKNAME")); result.put("avatar", text(row, "AVATAR")); result.put("phone", text(row, "PHONE")); result.put("verified", Boolean.TRUE.equals(row.get("VERIFIED"))); result.put("resume", hasResume ? resume : null);
    result.put("anchorCard", primaryCard);
    result.put("anchorCards", cards);
    result.put("cardCompleted", cardCompleted);
    return result;
  }

  public Map<String, Object> me(String token) {
    return profile(userId(token));
  }

  @Transactional
  public Map<String, Object> updateProfile(String token, Map<String, Object> input) {
    String uid = userId(token);
    jdbc.update("UPDATE app_user SET nickname=COALESCE(?,nickname),city=COALESCE(?,city),categories=COALESCE(?,categories),intro=COALESCE(?,intro),experience_years=COALESCE(?,experience_years) WHERE id=?", input.get("nickname"), input.get("city"), input.containsKey("categories") ? json(input.get("categories")) : null, input.get("intro"), input.get("experienceYears"), uid);
    return profile(uid);
  }

  @Transactional
  public Map<String, Object> updateAnchorCard(String token, Map<String, Object> input) {
    requireRole(token, "anchor");
    String uid = userId(token);
    migrateLegacyCard(one("SELECT * FROM app_user WHERE id=?", uid));
    String cardId = String.valueOf(input.getOrDefault("id", "")).trim();
    if (cardId.isBlank()) {
      var primary = primaryCardRow(uid);
      cardId = primary == null ? "" : text(primary, "ID");
    }
    return saveAnchorCard(uid, cardId, input);
  }

  @Transactional
  public Map<String, Object> createAnchorCard(String token, Map<String, Object> input) {
    requireRole(token, "anchor");
    return saveAnchorCard(userId(token), "", input);
  }

  @Transactional
  public Map<String, Object> updateAnchorCard(String token, String cardId, Map<String, Object> input) {
    requireRole(token, "anchor");
    return saveAnchorCard(userId(token), cardId, input);
  }

  @Transactional
  public Map<String, Object> deleteAnchorCard(String token, String cardId) {
    requireRole(token, "anchor");
    String uid = userId(token);
    migrateLegacyCard(one("SELECT * FROM app_user WHERE id=?", uid));
    var card = one("SELECT * FROM anchor_card WHERE id=? AND owner_id=? AND status='PUBLIC'", cardId, uid);
    if (card == null) throw new BusinessException("模卡不存在或无权操作");
    boolean wasPrimary = booleanValue(card.get("IS_PRIMARY"));
    jdbc.update("DELETE FROM anchor_card WHERE id=? AND owner_id=?", cardId, uid);
    if (wasPrimary) {
      var replacement = one("SELECT id FROM anchor_card WHERE owner_id=? AND status='PUBLIC' ORDER BY updated_at DESC", uid);
      if (replacement != null) jdbc.update("UPDATE anchor_card SET is_primary=TRUE,updated_at=? WHERE id=?", now(), text(replacement, "ID"));
    }
    syncPrimaryCardToUser(uid);
    return profile(uid);
  }

  @Transactional
  public Map<String, Object> setPrimaryAnchorCard(String token, String cardId) {
    requireRole(token, "anchor");
    String uid = userId(token);
    migrateLegacyCard(one("SELECT * FROM app_user WHERE id=?", uid));
    if (one("SELECT id FROM anchor_card WHERE id=? AND owner_id=? AND status='PUBLIC'", cardId, uid) == null) {
      throw new BusinessException("模卡不存在或无权操作");
    }
    long timestamp = now();
    jdbc.update("UPDATE anchor_card SET is_primary=FALSE WHERE owner_id=?", uid);
    jdbc.update("UPDATE anchor_card SET is_primary=TRUE,updated_at=? WHERE id=? AND owner_id=?", timestamp, cardId, uid);
    syncPrimaryCardToUser(uid);
    return profile(uid);
  }

  private Map<String, Object> saveAnchorCard(String uid, String cardId, Map<String, Object> input) {
    migrateLegacyCard(one("SELECT * FROM app_user WHERE id=?", uid));
    var existing = cardId == null || cardId.isBlank() ? null : one("SELECT * FROM anchor_card WHERE id=? AND owner_id=? AND status='PUBLIC'", cardId, uid);
    if (cardId != null && !cardId.isBlank() && existing == null) throw new BusinessException("模卡不存在或无权操作");
    if (existing == null && number(one("SELECT COUNT(*) AS total FROM anchor_card WHERE owner_id=? AND status='PUBLIC'", uid), "TOTAL") >= 5) {
      throw new BusinessException("最多创建 5 张模卡，请删除不再使用的模卡后再创建");
    }
    Map<String, Object> card = buildAnchorCard(input);
    long timestamp = now();
    if (existing == null) {
      String createdId = id("card");
      boolean isPrimary = primaryCardRow(uid) == null;
      jdbc.update("INSERT INTO anchor_card(id,owner_id,card_data,is_primary,status,created_at,updated_at) VALUES(?,?,?,?,?,?,?)", createdId, uid, json(card), isPrimary, "PUBLIC", timestamp, timestamp);
    } else {
      jdbc.update("UPDATE anchor_card SET card_data=?,updated_at=? WHERE id=? AND owner_id=?", json(card), timestamp, cardId, uid);
    }
    syncPrimaryCardToUser(uid);
    return profile(uid);
  }

  private Map<String, Object> buildAnchorCard(Map<String, Object> input) {
    String stageName = input.get("stageName") == null ? "" : String.valueOf(input.get("stageName")).trim();
    String city = input.get("city") == null ? "" : String.valueOf(input.get("city")).trim();
    String intro = input.get("intro") == null ? "" : String.valueOf(input.get("intro")).trim();
    List<String> categories = strings(input.get("categories")).stream().map(String::trim).filter(value -> !value.isBlank()).distinct().toList();
    int experienceYears;
    try { experienceYears = Integer.parseInt(String.valueOf(input.getOrDefault("experienceYears", 0))); } catch (Exception error) { experienceYears = -1; }
    if (stageName.isBlank() || city.isBlank() || intro.isBlank() || categories.isEmpty() || experienceYears < 0) {
      throw new BusinessException("请完善模卡必填项：艺名、品类、城市、经验和个人简介");
    }
    Map<String, Object> card = new LinkedHashMap<>();
    card.put("stageName", stageName);
    card.put("categories", categories);
    card.put("city", city);
    card.put("intro", intro);
    card.put("experienceYears", experienceYears);
    card.put("expectedSalary", String.valueOf(input.getOrDefault("expectedSalary", "面议")).trim());
    card.put("availableTime", String.valueOf(input.getOrDefault("availableTime", "时间可协商")).trim());
    card.put("age", numberValue(input.getOrDefault("age", 23), 23));
    card.put("gender", String.valueOf(input.getOrDefault("gender", "女")).trim());
    card.put("height", String.valueOf(input.getOrDefault("height", "166cm")).trim());
    card.put("weight", String.valueOf(input.getOrDefault("weight", "47kg")).trim());
    card.put("shoeSize", String.valueOf(input.getOrDefault("shoeSize", "37码")).trim());
    card.put("education", String.valueOf(input.getOrDefault("education", "本科及以上")).trim());
    List<String> expectedCities = strings(input.get("expectedCities")).stream().map(String::trim).filter(value -> !value.isBlank()).distinct().toList();
    card.put("expectedCities", expectedCities.isEmpty() ? List.of(city) : expectedCities);
    card.put("acceptShift", booleanValue(input.getOrDefault("acceptShift", false)));
    card.put("experienceCategory", String.valueOf(input.getOrDefault("experienceCategory", categories.get(0))).trim());
    card.put("accountName", String.valueOf(input.getOrDefault("accountName", "合****")).trim());
    card.put("peakGmv", String.valueOf(input.getOrDefault("peakGmv", "30万")).trim());
    card.put("liveYears", numberValue(input.getOrDefault("liveYears", experienceYears), experienceYears));
    card.put("advantage", String.valueOf(input.getOrDefault("advantage", intro)).trim());
    card.put("coverImage", String.valueOf(input.getOrDefault("coverImage", "")).trim());
    card.put("clips", strings(input.get("clips")));
    card.put("recordingUrl", String.valueOf(input.getOrDefault("recordingUrl", "")).trim());
    card.put("groupName", String.valueOf(input.getOrDefault("groupName", "主播招聘群")).trim());
    card.put("groupDescription", String.valueOf(input.getOrDefault("groupDescription", "免费招主播 · 免费进群")).trim());
    return card;
  }

  private List<Map<String, Object>> anchorCards(String uid) {
    return jdbc.queryForList("SELECT * FROM anchor_card WHERE owner_id=? AND status='PUBLIC' ORDER BY is_primary DESC,updated_at DESC", uid)
      .stream().map(this::anchorCard).toList();
  }

  private Map<String, Object> primaryCardRow(String uid) {
    return one("SELECT * FROM anchor_card WHERE owner_id=? AND status='PUBLIC' ORDER BY is_primary DESC,updated_at DESC", uid);
  }

  private Map<String, Object> anchorCard(Map<String, Object> row) {
    Map<String, Object> card = object(row.get("CARD_DATA"));
    card.put("id", text(row, "ID"));
    card.put("isPrimary", booleanValue(row.get("IS_PRIMARY")));
    card.put("createdAt", longValue(row.get("CREATED_AT")));
    card.put("updatedAt", longValue(row.get("UPDATED_AT")));
    return card;
  }

  private void migrateLegacyCard(Map<String, Object> userRow) {
    if (userRow == null || !"anchor".equalsIgnoreCase(text(userRow, "ROLE"))) return;
    String uid = text(userRow, "ID");
    if (one("SELECT id FROM anchor_card WHERE owner_id=?", uid) != null || !"COMPLETE".equalsIgnoreCase(text(userRow, "CARD_STATUS"))) return;
    Map<String, Object> legacyCard = object(userRow.get("CARD_DATA"));
    if (legacyCard.isEmpty()) return;
    long timestamp = now();
    jdbc.update("INSERT INTO anchor_card(id,owner_id,card_data,is_primary,status,created_at,updated_at) VALUES(?,?,?,?,?,?,?)", id("card"), uid, json(legacyCard), true, "PUBLIC", timestamp, timestamp);
  }

  private void syncPrimaryCardToUser(String uid) {
    var primary = primaryCardRow(uid);
    if (primary == null) {
      jdbc.update("UPDATE app_user SET card_status='INCOMPLETE',card_data='{}' WHERE id=?", uid);
      return;
    }
    Map<String, Object> card = anchorCard(primary);
    jdbc.update("UPDATE app_user SET nickname=?,city=?,categories=?,intro=?,experience_years=?,card_status='COMPLETE',card_data=? WHERE id=?",
      String.valueOf(card.getOrDefault("stageName", "主播")).trim(), String.valueOf(card.getOrDefault("city", "")).trim(), json(strings(card.get("categories"))),
      String.valueOf(card.getOrDefault("intro", "")).trim(), numberValue(card.getOrDefault("experienceYears", 0), 0), json(card), uid);
  }

  private int numberValue(Object value, int fallback) {
    try { return Integer.parseInt(String.valueOf(value)); } catch (Exception error) { return fallback; }
  }

  private boolean booleanValue(Object value) {
    if (value instanceof Boolean bool) return bool;
    return Boolean.parseBoolean(String.valueOf(value));
  }

  public Map<String, Object> uploadMedia(String token, MultipartFile file) {
    requireRole(token, "anchor");
    if (file == null || file.isEmpty()) throw new BusinessException("请选择要上传的录屏或图片");
    if (file.getSize() > 300L * 1024 * 1024) throw new BusinessException("上传文件不能超过 300MB");
    String originalName = file.getOriginalFilename() == null ? "media" : file.getOriginalFilename();
    String suffix = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.')).toLowerCase() : "";
    if (!List.of(".mp4", ".mov", ".m4v", ".webm", ".jpg", ".jpeg", ".png", ".webp").contains(suffix)) {
      throw new BusinessException("仅支持常见视频或图片格式");
    }
    String fileName = id("media") + suffix;
    Path root = Path.of("data", "uploads").toAbsolutePath().normalize();
    Path target = root.resolve(fileName).normalize();
    if (!target.startsWith(root)) throw new BusinessException("上传文件路径不正确");
    try {
      Files.createDirectories(root);
      Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
    } catch (Exception error) {
      throw new BusinessException("媒体文件保存失败");
    }
    return Map.of("path", "/uploads/" + fileName, "size", file.getSize(), "contentType", file.getContentType() == null ? "application/octet-stream" : file.getContentType());
  }

  public List<Map<String, Object>> talents(String token, Map<String, String> filter) {
    requireRole(token, "merchant");
    String keyword = filter.getOrDefault("keyword", "").trim().toLowerCase();
    String gender = filter.getOrDefault("gender", "").trim();
    String category = filter.getOrDefault("category", "").trim().toLowerCase();
    migrateLegacyCards();
    return jdbc.queryForList("SELECT u.id AS user_id,u.nickname,u.avatar,u.verified,c.id AS card_id,c.card_data,c.is_primary,c.created_at AS card_created_at,c.updated_at AS card_updated_at FROM app_user u JOIN anchor_card c ON c.owner_id=u.id AND c.status='PUBLIC' AND c.is_primary=TRUE WHERE u.role='anchor' ORDER BY c.updated_at DESC")
      .stream()
      .map(this::publicTalent)
      .filter(item -> {
        Map<String, Object> card = object(item.get("anchorCard"));
        String searchable = (String.valueOf(item.get("nickname")) + " " + card.values()).toLowerCase();
        if (!keyword.isBlank() && !searchable.contains(keyword)) return false;
        if (!gender.isBlank() && !gender.equals(String.valueOf(card.getOrDefault("gender", "")))) return false;
        return category.isBlank() || searchable.contains(category);
      })
      .toList();
  }

  public Map<String, Object> talent(String token, String talentId) {
    requireRole(token, "merchant");
    migrateLegacyCard(one("SELECT * FROM app_user WHERE id=? AND role='anchor'", talentId));
    var row = one("SELECT u.id AS user_id,u.nickname,u.avatar,u.verified,c.id AS card_id,c.card_data,c.is_primary,c.created_at AS card_created_at,c.updated_at AS card_updated_at FROM app_user u JOIN anchor_card c ON c.owner_id=u.id AND c.status='PUBLIC' AND c.is_primary=TRUE WHERE u.id=? AND u.role='anchor'", talentId);
    if (row == null) throw new BusinessException("主播模卡不存在或暂未公开");
    return publicTalent(row);
  }

  private Map<String, Object> publicTalent(Map<String, Object> row) {
    Map<String, Object> card = object(row.get("CARD_DATA"));
    card.put("id", text(row, "CARD_ID"));
    card.put("isPrimary", booleanValue(row.get("IS_PRIMARY")));
    card.put("createdAt", longValue(row.get("CARD_CREATED_AT")));
    card.put("updatedAt", longValue(row.get("CARD_UPDATED_AT")));
    card.putIfAbsent("stageName", text(row, "NICKNAME"));
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("id", text(row, "USER_ID"));
    result.put("nickname", text(row, "NICKNAME"));
    result.put("avatar", text(row, "AVATAR"));
    result.put("verified", Boolean.TRUE.equals(row.get("VERIFIED")));
    result.put("activeLabel", "3日内活跃");
    result.put("anchorCard", card);
    return result;
  }

  private void requireAnchorCard(String token) {
    requireRole(token, "anchor");
    String uid = userId(token);
    migrateLegacyCard(one("SELECT * FROM app_user WHERE id=?", uid));
    if (one("SELECT id FROM anchor_card WHERE owner_id=? AND status='PUBLIC'", uid) == null) {
      throw new BusinessException("请先完善主播模卡，再使用该功能");
    }
  }

  private void migrateLegacyCards() {
    jdbc.queryForList("SELECT * FROM app_user WHERE role='anchor' AND card_status='COMPLETE'").forEach(this::migrateLegacyCard);
  }

  @Transactional
  public Map<String, Object> verify(String token, Map<String, Object> input) {
    requireRole(token, "anchor");
    String uid = userId(token);
    if (String.valueOf(input.getOrDefault("realName", "")).isBlank()) throw new BusinessException("请填写实名信息");
    jdbc.update("UPDATE app_user SET verified=TRUE WHERE id=?", uid);
    return Map.of("verified", true, "status", "APPROVED", "message", "实名认证资料已进入审核队列");
  }

  public List<Map<String, Object>> notices(Map<String, String> filter) {
    StringBuilder sql = new StringBuilder("SELECT * FROM job_notice WHERE status<>'DELETED'");
    List<Object> args = new ArrayList<>();
    if (filter.get("keyword") != null && !filter.get("keyword").isBlank()) { sql.append(" AND (LOWER(title) LIKE LOWER(?) OR LOWER(city) LIKE LOWER(?) OR LOWER(publisher_name) LIKE LOWER(?))"); String kw = "%" + filter.get("keyword") + "%"; args.add(kw); args.add(kw); args.add(kw); }
    if (filter.get("city") != null && !filter.get("city").isBlank()) { sql.append(" AND city=?"); args.add(filter.get("city")); }
    if (filter.get("category") != null && !filter.get("category").isBlank()) { sql.append(" AND category=?"); args.add(filter.get("category")); }
    if (filter.get("jobType") != null && !filter.get("jobType").isBlank()) { sql.append(" AND job_type=?"); args.add(filter.get("jobType")); }
    sql.append(" ORDER BY urgent DESC, published_at DESC");
    return jdbc.queryForList(sql.toString(), args.toArray()).stream().map(this::notice).toList();
  }

  public Map<String, Object> noticeById(String noticeId) {
    var row = one("SELECT * FROM job_notice WHERE id=? AND status<>'DELETED'", noticeId);
    if (row == null) throw new BusinessException("通告不存在或已下架");
    jdbc.update("UPDATE job_notice SET view_count=view_count+1 WHERE id=?", noticeId);
    return notice(one("SELECT * FROM job_notice WHERE id=?", noticeId));
  }

  private Map<String, Object> notice(Map<String, Object> row) {
    Map<String, Object> publisher = new LinkedHashMap<>();
    publisher.put("id", text(row, "PUBLISHER_ID")); publisher.put("name", text(row, "PUBLISHER_NAME")); publisher.put("avatar", text(row, "PUBLISHER_AVATAR"));
    publisher.put("verification", Map.of("realName", Boolean.TRUE.equals(row.get("PUBLISHER_REAL_NAME")), "enterprise", Boolean.TRUE.equals(row.get("PUBLISHER_ENTERPRISE"))));
    Map<String, Object> salary = Map.of("min", row.get("SALARY_MIN"), "max", row.get("SALARY_MAX"), "unit", text(row, "SALARY_UNIT"), "display", text(row, "SALARY_DISPLAY"));
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("id", text(row, "ID")); result.put("title", text(row, "TITLE")); result.put("jobType", text(row, "JOB_TYPE")); result.put("category", text(row, "CATEGORY")); result.put("salary", salary);
    result.put("city", text(row, "CITY")); result.put("address", text(row, "ADDRESS")); result.put("distanceKm", row.get("DISTANCE_KM")); result.put("longitude", row.get("LONGITUDE")); result.put("latitude", row.get("LATITUDE"));
    result.put("duties", strings(row.get("DUTIES"))); result.put("requirements", strings(row.get("REQUIREMENTS"))); result.put("tags", strings(row.get("TAGS"))); result.put("publisher", publisher);
    result.put("urgent", Boolean.TRUE.equals(row.get("URGENT"))); result.put("publishedAt", row.get("PUBLISHED_AT")); result.put("viewCount", number(row, "VIEW_COUNT")); result.put("status", text(row, "STATUS")); result.put("applyCount", number(row, "APPLY_COUNT"));
    return result;
  }

  private String myNoticeStatus(String status) {
    return switch (status) {
      case "DRAFT" -> "draft";
      case "PENDING" -> "pending";
      case "REJECTED" -> "rejected";
      default -> "published";
    };
  }

  private Map<String, Object> myNotice(Map<String, Object> row) {
    Map<String, Object> result = new LinkedHashMap<>(notice(row));
    result.put("status", myNoticeStatus(text(row, "STATUS")));
    result.put("createdAt", row.get("PUBLISHED_AT"));
    result.put("updatedAt", row.get("PUBLISHED_AT"));
    result.put("rejectReason", "REJECTED".equals(text(row, "STATUS")) ? "通告内容需要补充，请修改后重新提交" : null);
    return result;
  }

  @Transactional
  public Map<String, Object> createNotice(String token, Map<String, Object> input) {
    requireRole(token, "merchant"); String uid = userId(token); String noticeId = id("n");
    var user = one("SELECT nickname,city FROM app_user WHERE id=?", uid);
    String title = String.valueOf(input.getOrDefault("title", "未命名通告"));
    String salaryDisplay = String.valueOf(input.getOrDefault("salaryDisplay", "面议"));
    jdbc.update("INSERT INTO job_notice(id,title,job_type,category,salary_min,salary_max,salary_unit,salary_display,city,address,distance_km,longitude,latitude,duties,requirements,tags,publisher_id,publisher_name,publisher_avatar,publisher_real_name,publisher_enterprise,urgent,published_at,view_count,status,apply_count) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", noticeId, title, input.getOrDefault("jobType", "full-time"), input.getOrDefault("category", "live-commerce"), input.getOrDefault("salaryMin", 0), input.getOrDefault("salaryMax", 0), input.getOrDefault("salaryUnit", "month"), salaryDisplay, input.getOrDefault("city", text(user, "CITY")), input.getOrDefault("address", ""), input.getOrDefault("distanceKm", 0), input.getOrDefault("longitude", 0), input.getOrDefault("latitude", 0), json(input.getOrDefault("duties", List.of())), json(input.getOrDefault("requirements", List.of())), json(input.getOrDefault("tags", List.of())), uid, text(user, "NICKNAME"), "", Boolean.TRUE.equals(user == null ? false : user.get("VERIFIED")), true, Boolean.TRUE.equals(input.get("urgent")), now(), 0, "DRAFT", 0);
    return notice(one("SELECT * FROM job_notice WHERE id=?", noticeId));
  }

  @Transactional
  public Map<String, Object> updateNotice(String token, String noticeId, Map<String, Object> input) {
    requireRole(token, "merchant");
    String uid = userId(token);
    var existing = one("SELECT * FROM job_notice WHERE id=? AND publisher_id=?", noticeId, uid);
    if (existing == null) throw new BusinessException("通告不存在或无权编辑");
    jdbc.update("UPDATE job_notice SET title=?,job_type=?,category=?,salary_min=?,salary_max=?,salary_unit=?,salary_display=?,city=?,address=?,duties=?,requirements=?,tags=?,urgent=?,published_at=? WHERE id=?",
      input.getOrDefault("title", text(existing, "TITLE")), input.getOrDefault("jobType", text(existing, "JOB_TYPE")), input.getOrDefault("category", text(existing, "CATEGORY")),
      input.getOrDefault("salaryMin", existing.get("SALARY_MIN")), input.getOrDefault("salaryMax", existing.get("SALARY_MAX")), input.getOrDefault("salaryUnit", text(existing, "SALARY_UNIT")), input.getOrDefault("salaryDisplay", text(existing, "SALARY_DISPLAY")),
      input.getOrDefault("city", text(existing, "CITY")), input.getOrDefault("address", text(existing, "ADDRESS")), json(input.getOrDefault("duties", strings(existing.get("DUTIES")))), json(input.getOrDefault("requirements", strings(existing.get("REQUIREMENTS")))), json(input.getOrDefault("tags", strings(existing.get("TAGS")))), Boolean.TRUE.equals(input.getOrDefault("urgent", existing.get("URGENT"))), now(), noticeId);
    return notice(one("SELECT * FROM job_notice WHERE id=?", noticeId));
  }

  public List<Map<String, Object>> myNotices(String token, String status) {
    requireRole(token, "merchant"); String uid = userId(token);
    var rows = jdbc.queryForList("SELECT * FROM job_notice WHERE publisher_id=? AND status<>'DELETED' ORDER BY published_at DESC", uid);
    return rows.stream().map(this::myNotice).filter(row -> status == null || status.isBlank() || status.equals(row.get("status"))).toList();
  }

  @Transactional
  public Map<String, Object> noticeAction(String token, String noticeId, String action) {
    String ownerId = "";
    if (token != null && !token.isBlank()) {
      requireRole(token, "merchant");
      ownerId = userId(token);
    }
    var row = one("SELECT * FROM job_notice WHERE id=?", noticeId);
    if (row == null) throw new BusinessException("通告不存在");
    if (!ownerId.isBlank() && !ownerId.equals(text(row, "PUBLISHER_ID"))) throw new BusinessException("只能操作自己发布的通告");
    String status = switch (action) { case "publish" -> "PENDING"; case "approve" -> "PUBLISHED"; case "offline" -> "OFFLINE"; case "reject" -> "REJECTED"; default -> throw new BusinessException("不支持的通告操作"); };
    jdbc.update("UPDATE job_notice SET status=? WHERE id=?", status, noticeId);
    return notice(one("SELECT * FROM job_notice WHERE id=?", noticeId));
  }

  public Map<String, Object> wallet(String token) {
    String uid = userId(token); var row = one("SELECT * FROM user_wallet WHERE user_id=?", uid); if (row == null) throw new BusinessException("钱包不存在");
    return Map.of("userId", uid, "cardBalance", number(row, "CARD_BALANCE"), "memberLevel", text(row, "MEMBER_LEVEL"), "aiQuota", number(row, "AI_QUOTA"));
  }

  @Transactional
  public Map<String, Object> topup(String token, Map<String, Object> input) {
    String uid = userId(token); int cards = ((Number) input.getOrDefault("cards", 1)).intValue(); if (cards < 1) throw new BusinessException("购买数量不正确");
    jdbc.update("UPDATE user_wallet SET card_balance=card_balance+?,updated_at=? WHERE user_id=?", cards, now(), uid);
    return Map.of("orderId", id("pay"), "status", "PAID_SANDBOX", "wallet", wallet(token), "message", "本地支付沙箱已完成");
  }

  @Transactional
  public Map<String, Object> unlock(String token, String noticeId) {
    requireAnchorCard(token); String uid = userId(token); var existing = one("SELECT id FROM contact_unlock WHERE user_id=? AND job_id=?", uid, noticeId); if (existing != null) return Map.of("unlocked", true, "already", true);
    var wallet = one("SELECT card_balance FROM user_wallet WHERE user_id=?", uid); if (number(wallet, "CARD_BALANCE") < 1) throw new BusinessException("道具卡余额不足，请先补充");
    jdbc.update("UPDATE user_wallet SET card_balance=card_balance-1,updated_at=? WHERE user_id=?", now(), uid); jdbc.update("INSERT INTO contact_unlock(id,user_id,job_id,cost,created_at) VALUES(?,?,?,?,?)", id("unlock"), uid, noticeId, 1, now());
    var row = one("SELECT publisher_name,publisher_id FROM job_notice WHERE id=?", noticeId); if (row == null) throw new BusinessException("通告不存在");
    return Map.of("unlocked", true, "company", text(row, "PUBLISHER_NAME"), "publisherId", text(row, "PUBLISHER_ID"), "wallet", wallet(token));
  }

  @Transactional
  public Map<String, Object> aiScript(String token, Map<String, Object> input) {
    requireAnchorCard(token); String uid = userId(token); var wallet = one("SELECT ai_quota FROM user_wallet WHERE user_id=?", uid); if (number(wallet, "AI_QUOTA") < 1) throw new BusinessException("AI 额度不足，请升级会员");
    String product = String.valueOf(input.getOrDefault("product", "直播商品")); String scene = String.valueOf(input.getOrDefault("scene", "开场介绍")); String tone = String.valueOf(input.getOrDefault("tone", "亲和专业"));
    String content = "【" + scene + "】\n姐妹们，今天给大家分享" + product + "。适合想要快速上手、追求品质和性价比的朋友，喜欢就先收藏，直播间还有专属福利。";
    jdbc.update("UPDATE user_wallet SET ai_quota=ai_quota-1,updated_at=? WHERE user_id=?", now(), uid); String sid = id("ai"); jdbc.update("INSERT INTO ai_script(id,user_id,scene,product,tone,content,created_at) VALUES(?,?,?,?,?,?,?)", sid, uid, scene, product, tone, content, now());
    return Map.of("id", sid, "scene", scene, "product", product, "tone", tone, "content", content, "remainingQuota", number(one("SELECT ai_quota FROM user_wallet WHERE user_id=?", uid), "AI_QUOTA"), "provider", "LOCAL_SANDBOX");
  }

  public List<Map<String, Object>> aiScripts(String token) {
    return jdbc.queryForList("SELECT id,scene,product,tone,content,created_at FROM ai_script WHERE user_id=? ORDER BY created_at DESC", userId(token))
      .stream().map(row -> Map.<String, Object>of(
        "id", text(row, "ID"), "scene", text(row, "SCENE"), "product", text(row, "PRODUCT"),
        "tone", text(row, "TONE"), "content", text(row, "CONTENT"), "createdAt", row.get("CREATED_AT")
      )).toList();
  }

  @Transactional
  public Map<String, Object> membership(String token, Map<String, Object> input) {
    String uid = userId(token); String plan = String.valueOf(input.getOrDefault("plan", "PRO")); BigDecimal amount = new BigDecimal(String.valueOf(input.getOrDefault("amount", "29.9"))); String oid = id("member");
    jdbc.update("INSERT INTO membership_order(id,user_id,plan,amount,status,created_at) VALUES(?,?,?,?,?,?)", oid, uid, plan, amount, "PAID_SANDBOX", now()); jdbc.update("UPDATE user_wallet SET member_level=?,ai_quota=ai_quota+50,updated_at=? WHERE user_id=?", plan, now(), uid);
    return Map.of("orderId", oid, "plan", plan, "status", "PAID_SANDBOX", "wallet", wallet(token));
  }

  public List<Map<String, Object>> conversations() {
    return jdbc.queryForList("SELECT id,role,name,avatar,last_message,last_time,unread FROM conversation ORDER BY last_time DESC")
      .stream().map(row -> Map.<String, Object>of(
        "id", text(row, "ID"), "role", text(row, "ROLE"), "name", text(row, "NAME"), "avatar", text(row, "AVATAR"),
        "lastMessage", text(row, "LAST_MESSAGE"), "lastTime", row.get("LAST_TIME"), "unread", number(row, "UNREAD")
      )).toList();
  }
  private Map<String, Object> message(Map<String, Object> row) {
    return Map.of("id", text(row, "ID"), "conversationId", text(row, "CONVERSATION_ID"), "content", text(row, "CONTENT"),
      "type", text(row, "MESSAGE_TYPE"), "fromMe", Boolean.TRUE.equals(row.get("FROM_ME")), "createdAt", row.get("CREATED_AT"));
  }
  public List<Map<String, Object>> messages(String conversationId) {
    return jdbc.queryForList("SELECT id,conversation_id,content,message_type,from_me,created_at FROM chat_message WHERE conversation_id=? ORDER BY created_at", conversationId)
      .stream().map(this::message).toList();
  }

  @Transactional
  public Map<String, Object> sendMessage(String token, String conversationId, Map<String, Object> input) {
    String content = String.valueOf(input.getOrDefault("content", "")).trim(); if (content.isBlank()) throw new BusinessException("消息不能为空");
    requireAnchorCard(token); String uid = userId(token); var quota = one("SELECT remaining_count FROM message_quota WHERE user_id=?", uid); if (number(quota, "REMAINING_COUNT") < 1) throw new BusinessException("今日沟通次数已用完");
    String mid = id("msg"); long timestamp = now(); jdbc.update("UPDATE message_quota SET remaining_count=remaining_count-1 WHERE user_id=?", uid); jdbc.update("INSERT INTO chat_message(id,conversation_id,content,message_type,from_me,created_at) VALUES(?,?,?,?,?,?)", mid, conversationId, content, "text", true, timestamp); jdbc.update("UPDATE conversation SET last_message=?,last_time=? WHERE id=?", content, timestamp, conversationId); return message(one("SELECT id,conversation_id,content,message_type,from_me,created_at FROM chat_message WHERE id=?", mid));
  }

  public Map<String, Object> quota(String token) { String uid = userId(token); var row = one("SELECT remaining_count,total_count FROM message_quota WHERE user_id=?", uid); return Map.of("remaining", number(row, "REMAINING_COUNT"), "total", number(row, "TOTAL_COUNT")); }

  @Transactional
  public Map<String, Object> createContract(Map<String, Object> input) { String cid=id("ct"); jdbc.update("INSERT INTO employment_contract(id,anchor_name,company,job_title,amount,status,created_at) VALUES(?,?,?,?,?,?,?)", cid, input.getOrDefault("anchorName", "主播"), input.getOrDefault("company", "招聘企业"), input.getOrDefault("jobTitle", "主播"), input.getOrDefault("amount", 0), "DRAFT", now()); return one("SELECT * FROM employment_contract WHERE id=?", cid); }
  @Transactional
  public Map<String, Object> createAnchorContract(String token, Map<String, Object> input) { requireAnchorCard(token); return createContract(input); }
  public List<Map<String,Object>> contracts() { return jdbc.queryForList("SELECT * FROM employment_contract ORDER BY created_at DESC"); }
  @Transactional
  public Map<String,Object> settle(String contractId, Map<String,Object> input) { var contract=one("SELECT * FROM employment_contract WHERE id=?", contractId); if(contract==null) throw new BusinessException("合同不存在"); BigDecimal gross=new BigDecimal(String.valueOf(input.getOrDefault("grossAmount", contract.get("AMOUNT")))); BigDecimal fee=gross.multiply(new BigDecimal("0.06")).setScale(2,RoundingMode.HALF_UP); BigDecimal net=gross.subtract(fee); String sid=id("set"); jdbc.update("INSERT INTO settlement(id,contract_id,currency,gross_amount,service_fee,net_amount,status,created_at) VALUES(?,?,?,?,?,?,?,?)", sid,contractId,input.getOrDefault("currency","CNY"),gross,fee,net,"SETTLED_SANDBOX",now()); return one("SELECT * FROM settlement WHERE id=?",sid); }
  @Transactional
  public Map<String,Object> settleAnchorContract(String token, String contractId, Map<String,Object> input) { requireAnchorCard(token); return settle(contractId, input); }
  public List<Map<String,Object>> settlements() { return jdbc.queryForList("SELECT * FROM settlement ORDER BY created_at DESC"); }
  @Transactional
  public Map<String,Object> invitation(Map<String,Object> input) { String id=id("invite"); jdbc.update("INSERT INTO paid_invitation(id,company,anchor_name,job_title,fee,status,created_at) VALUES(?,?,?,?,?,?,?)",id,input.getOrDefault("company","招聘企业"),input.getOrDefault("anchorName","主播"),input.getOrDefault("jobTitle","主播"),input.getOrDefault("fee",29.9),"PAID_SANDBOX",now()); return one("SELECT * FROM paid_invitation WHERE id=?",id); }
  public List<Map<String,Object>> invitations() { return jdbc.queryForList("SELECT * FROM paid_invitation ORDER BY created_at DESC"); }

  public List<Map<String,Object>> courses(String mode) { return mode == null || mode.isBlank() ? jdbc.queryForList("SELECT * FROM course ORDER BY starts_at") : jdbc.queryForList("SELECT * FROM course WHERE mode=? ORDER BY starts_at", mode); }
  @Transactional
  public Map<String,Object> enroll(String token,String courseId) { requireAnchorCard(token); String uid=userId(token); var course=one("SELECT * FROM course WHERE id=?",courseId); if(course==null) throw new BusinessException("课程不存在"); if(number(course,"ENROLLED")>=number(course,"CAPACITY")) throw new BusinessException("课程名额已满"); String eid=id("enroll"); jdbc.update("INSERT INTO course_enrollment(id,course_id,user_id,status,score,certificate_no,created_at) VALUES(?,?,?,?,?,?,?)",eid,courseId,uid,"ENROLLED",null,null,now()); jdbc.update("UPDATE course SET enrolled=enrolled+1 WHERE id=?",courseId); return one("SELECT * FROM course_enrollment WHERE id=?",eid); }
  @Transactional
  public Map<String,Object> exam(String token,String enrollmentId,Map<String,Object> input) { requireAnchorCard(token); int score=((Number)input.getOrDefault("score",0)).intValue(); String status=score>=60?"PASSED":"RETAKE"; String cert=score>=60?"BP-"+now():null; jdbc.update("UPDATE course_enrollment SET score=?,status=?,certificate_no=? WHERE id=? AND user_id=?",score,status,cert,enrollmentId,userId(token)); return one("SELECT * FROM course_enrollment WHERE id=?",enrollmentId); }
  public List<Map<String,Object>> enrollments(String token) { return jdbc.queryForList("SELECT ce.*,c.name,c.mode,c.city FROM course_enrollment ce JOIN course c ON c.id=ce.course_id WHERE ce.user_id=? ORDER BY ce.created_at DESC",userId(token)); }

  public List<Map<String,Object>> products() { return jdbc.queryForList("SELECT * FROM equipment_product WHERE status='ON_SALE'"); }
  @Transactional
  public Map<String,Object> orderProduct(String token,String productId,Map<String,Object> input) { requireAnchorCard(token); var p=one("SELECT * FROM equipment_product WHERE id=?",productId); if(p==null||number(p,"STOCK")<1) throw new BusinessException("商品库存不足"); BigDecimal amount=new BigDecimal(String.valueOf(p.get("GROUP_PRICE"))); String oid=id("order"); jdbc.update("INSERT INTO platform_order(id,order_type,item_id,user_id,amount,currency,status,created_at) VALUES(?,?,?,?,?,?,?,?)",oid,"EQUIPMENT",productId,userId(token),amount,"CNY","PAID_SANDBOX",now()); jdbc.update("UPDATE equipment_product SET stock=stock-1,participants=participants+1 WHERE id=?",productId); return one("SELECT * FROM platform_order WHERE id=?",oid); }
  public List<Map<String,Object>> orders(String token) { return jdbc.queryForList("SELECT * FROM platform_order WHERE user_id=? ORDER BY created_at DESC",userId(token)); }

  public List<Map<String,Object>> events() { return jdbc.queryForList("SELECT * FROM annual_event ORDER BY event_date"); }
  @Transactional
  public Map<String,Object> registerEvent(String token,String eventId) { requireAnchorCard(token); String rid=id("eventreg"); try { jdbc.update("INSERT INTO event_registration(id,event_id,user_id,status,votes,created_at) VALUES(?,?,?,?,?,?)",rid,eventId,userId(token),"REGISTERED",0,now()); } catch(Exception error) { throw new BusinessException("你已经报名过该活动"); } return one("SELECT * FROM event_registration WHERE id=?",rid); }
  @Transactional
  public Map<String,Object> vote(String registrationId) { jdbc.update("UPDATE event_registration SET votes=votes+1 WHERE id=?",registrationId); return one("SELECT * FROM event_registration WHERE id=?",registrationId); }
  public List<Map<String,Object>> eventRegistrations(String eventId) { return jdbc.queryForList("SELECT * FROM event_registration WHERE event_id=? ORDER BY votes DESC",eventId); }

  @Transactional
  public Map<String,Object> crossBorder(String token,Map<String,Object> input) { requireAnchorCard(token); BigDecimal foreign=new BigDecimal(String.valueOf(input.getOrDefault("foreignAmount",0))); BigDecimal rate=new BigDecimal(String.valueOf(input.getOrDefault("rate",7.24))); BigDecimal cny=foreign.multiply(rate).setScale(2,RoundingMode.HALF_UP); BigDecimal fee=cny.multiply(new BigDecimal("0.015")).setScale(2,RoundingMode.HALF_UP); String sid=id("fx"); jdbc.update("INSERT INTO crossborder_settlement(id,user_id,country,currency,foreign_amount,rate,cny_amount,fee,net_cny,status,created_at) VALUES(?,?,?,?,?,?,?,?,?,?,?)",sid,userId(token),input.getOrDefault("country","新加坡"),input.getOrDefault("currency","USD"),foreign,rate,cny,fee,cny.subtract(fee),"SETTLED_SANDBOX",now()); return one("SELECT * FROM crossborder_settlement WHERE id=?",sid); }
  public List<Map<String,Object>> crossBorders(String token) { return jdbc.queryForList("SELECT * FROM crossborder_settlement WHERE user_id=? ORDER BY created_at DESC",userId(token)); }
  public List<Map<String,Object>> providers(String country) { return country==null||country.isBlank()?jdbc.queryForList("SELECT * FROM eor_provider WHERE status='ACTIVE' ORDER BY rating DESC"):jdbc.queryForList("SELECT * FROM eor_provider WHERE status='ACTIVE' AND country=? ORDER BY rating DESC",country); }
  @Transactional
  public Map<String,Object> eorRequest(Map<String,Object> input) { String rid=id("eor"); jdbc.update("INSERT INTO eor_request(id,provider_id,company,candidate,country,status,created_at) VALUES(?,?,?,?,?,?,?)",rid,input.getOrDefault("providerId","eor_sg_1"),input.getOrDefault("company","招聘企业"),input.getOrDefault("candidate","主播"),input.getOrDefault("country","新加坡"),"SUBMITTED_SANDBOX",now()); return one("SELECT * FROM eor_request WHERE id=?",rid); }
  public List<Map<String,Object>> eorRequests() { return jdbc.queryForList("SELECT * FROM eor_request ORDER BY created_at DESC"); }

  public Map<String,Object> adminOverview() {
    return Map.of(
      "pendingNotices", number(one("SELECT COUNT(*) AS total FROM job_notice WHERE status='PENDING'"), "TOTAL"),
      "newAnchors", number(one("SELECT COUNT(*) AS total FROM app_user WHERE role='anchor' AND created_at>?", now() - 24L * 60 * 60 * 1000), "TOTAL"),
      "pendingMessages", number(one("SELECT COALESCE(SUM(unread),0) AS total FROM conversation"), "TOTAL"),
      "matchedToday", number(one("SELECT COUNT(*) AS total FROM employment_contract WHERE status='ACTIVE'"), "TOTAL")
    );
  }

  public Map<String, Object> adminExport() {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("anchors", jdbc.queryForList("SELECT id,nickname,verified,city,categories,card_status,card_data,created_at FROM app_user WHERE role='anchor' ORDER BY created_at DESC"));
    result.put("notices", notices(Map.of()));
    result.put("contactUnlocks", jdbc.queryForList("SELECT * FROM contact_unlock ORDER BY created_at DESC"));
    result.put("membershipOrders", jdbc.queryForList("SELECT * FROM membership_order ORDER BY created_at DESC"));
    result.put("aiScripts", jdbc.queryForList("SELECT id,user_id,scene,product,tone,created_at FROM ai_script ORDER BY created_at DESC"));
    result.put("contracts", contracts());
    result.put("settlements", settlements());
    result.put("invitations", invitations());
    result.put("courses", courses(null));
    result.put("courseEnrollments", jdbc.queryForList("SELECT * FROM course_enrollment ORDER BY created_at DESC"));
    result.put("products", products());
    result.put("equipmentOrders", jdbc.queryForList("SELECT * FROM platform_order WHERE order_type='EQUIPMENT' ORDER BY created_at DESC"));
    result.put("events", events());
    result.put("eventRegistrations", jdbc.queryForList("SELECT * FROM event_registration ORDER BY created_at DESC"));
    result.put("crossBorderSettlements", jdbc.queryForList("SELECT * FROM crossborder_settlement ORDER BY created_at DESC"));
    result.put("eorProviders", providers(null));
    result.put("eorRequests", eorRequests());
    result.put("overview", adminOverview());
    return result;
  }
}
