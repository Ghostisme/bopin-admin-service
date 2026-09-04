package com.bopin.admin;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*", "http://198.18.0.1:*"})
public class AdminController {
  private final AdminPlatformService service;
  public AdminController(AdminPlatformService service) { this.service = service; }
  @GetMapping("/health")
  public Map<String, String> health() { return Map.of("status", "ok", "service", "bopin-admin-server"); }

  @GetMapping("/overview")
  public Map<String, Object> overview() {
    return service.adminOverview();
  }

  @GetMapping("/notices")
  public Map<String, Object> notices(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
    var pageResult = service.noticePage(Map.of("page", String.valueOf(page), "pageSize", String.valueOf(size)));
    @SuppressWarnings("unchecked")
    var all = (List<Map<String, Object>>) pageResult.get("items");
    var rows = all.stream().map(row -> Map.of(
      "id", row.get("id"),
      "title", row.get("title"),
      "company", ((Map<?, ?>) row.get("publisher")).get("name"),
      "city", row.get("city"),
      "salary", ((Map<?, ?>) row.get("salary")).get("display"),
      "status", row.get("status")
    )).toList();
    return Map.of("page", pageResult.get("page"), "size", pageResult.get("pageSize"), "total", pageResult.get("total"), "items", rows);
  }

}
