package com.bopin.admin;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {"http://localhost:5174", "http://127.0.0.1:5174", "http://198.18.0.1:5174", "http://localhost:10086", "http://127.0.0.1:10086"})
public class AdminPlatformController {
  private final AdminPlatformService service;
  public AdminPlatformController(AdminPlatformService service) { this.service = service; }
  private String token(String value) { return value == null ? "" : value.replaceFirst("^Bearer\\s+", ""); }

  @PostMapping("/auth/register") public ApiResult<Map<String,Object>> register(@RequestBody Map<String,Object> input) { return ApiResult.ok(service.register(input)); }
  @PostMapping("/auth/login") public ApiResult<Map<String,Object>> login(@RequestBody Map<String,Object> input) { return ApiResult.ok(service.login(input)); }
  @GetMapping("/users/me") public ApiResult<Map<String,Object>> profile(@RequestHeader(value="Authorization", required=false) String auth) { return ApiResult.ok(service.me(token(auth))); }
  @PutMapping("/users/me") public ApiResult<Map<String,Object>> updateProfile(@RequestHeader(value="Authorization", required=false) String auth,@RequestBody Map<String,Object> input) { return ApiResult.ok(service.updateProfile(token(auth),input)); }
  @PostMapping("/users/me/cards") public ApiResult<Map<String,Object>> createCard(@RequestHeader(value="Authorization", required=false) String auth,@RequestBody Map<String,Object> input) { return ApiResult.ok(service.createAnchorCard(token(auth),input)); }
  @PutMapping("/users/me/cards/{id}") public ApiResult<Map<String,Object>> updateCardById(@RequestHeader(value="Authorization", required=false) String auth,@PathVariable String id,@RequestBody Map<String,Object> input) { return ApiResult.ok(service.updateAnchorCard(token(auth),id,input)); }
  @DeleteMapping("/users/me/cards/{id}") public ApiResult<Map<String,Object>> deleteCard(@RequestHeader(value="Authorization", required=false) String auth,@PathVariable String id) { return ApiResult.ok(service.deleteAnchorCard(token(auth),id)); }
  @PostMapping("/users/me/cards/{id}/primary") public ApiResult<Map<String,Object>> setPrimaryCard(@RequestHeader(value="Authorization", required=false) String auth,@PathVariable String id) { return ApiResult.ok(service.setPrimaryAnchorCard(token(auth),id)); }
  // 旧版本客户端仍通过单张模卡地址保存，服务端会更新当前主展示模卡。
  @PutMapping("/users/me/card") public ApiResult<Map<String,Object>> updateCard(@RequestHeader(value="Authorization", required=false) String auth,@RequestBody Map<String,Object> input) { return ApiResult.ok(service.updateAnchorCard(token(auth),input)); }
  @PostMapping("/uploads/media") public ApiResult<Map<String,Object>> uploadMedia(@RequestHeader(value="Authorization", required=false) String auth,@RequestParam("file") MultipartFile file) { return ApiResult.ok(service.uploadMedia(token(auth),file)); }
  @PostMapping("/users/me/verify") public ApiResult<Map<String,Object>> verify(@RequestHeader(value="Authorization", required=false) String auth,@RequestBody Map<String,Object> input) { return ApiResult.ok(service.verify(token(auth),input)); }
  @GetMapping("/talents") public ApiResult<List<Map<String,Object>>> talents(@RequestHeader(value="Authorization", required=false) String auth,@RequestParam Map<String,String> filter) { return ApiResult.ok(service.talents(token(auth),filter)); }
  @GetMapping("/talents/{id}") public ApiResult<Map<String,Object>> talent(@RequestHeader(value="Authorization", required=false) String auth,@PathVariable String id) { return ApiResult.ok(service.talent(token(auth),id)); }
  @GetMapping("/notices") public ApiResult<List<Map<String,Object>>> notices(@RequestParam Map<String,String> filter) { return ApiResult.ok(service.notices(filter)); }
  @GetMapping("/notices/{id}") public ApiResult<Map<String,Object>> notice(@PathVariable String id) { return ApiResult.ok(service.noticeById(id)); }
  @PostMapping("/notices") public ApiResult<Map<String,Object>> createNotice(@RequestHeader(value="Authorization", required=false) String auth,@RequestBody Map<String,Object> input) { return ApiResult.ok(service.createNotice(token(auth),input)); }
  @PutMapping("/notices/{id}") public ApiResult<Map<String,Object>> updateNotice(@RequestHeader(value="Authorization", required=false) String auth,@PathVariable String id,@RequestBody Map<String,Object> input) { return ApiResult.ok(service.updateNotice(token(auth),id,input)); }
  @GetMapping("/notice/my-list") public ApiResult<List<Map<String,Object>>> myNotices(@RequestHeader(value="Authorization", required=false) String auth,@RequestParam(required=false) String status) { return ApiResult.ok(service.myNotices(token(auth),status)); }
  @PostMapping("/notices/{id}/{action}") public ApiResult<Map<String,Object>> noticeAction(@RequestHeader(value="Authorization", required=false) String auth,@PathVariable String id,@PathVariable String action) { return ApiResult.ok(service.noticeAction(token(auth),id,action)); }
  @PostMapping("/notices/{id}/unlock") public ApiResult<Map<String,Object>> unlock(@RequestHeader(value="Authorization", required=false) String auth,@PathVariable String id) { return ApiResult.ok(service.unlock(token(auth),id)); }

  @GetMapping("/wallet") public ApiResult<Map<String,Object>> wallet(@RequestHeader(value="Authorization", required=false) String auth) { return ApiResult.ok(service.wallet(token(auth))); }
  @PostMapping("/wallet/topup") public ApiResult<Map<String,Object>> topup(@RequestHeader(value="Authorization", required=false) String auth,@RequestBody Map<String,Object> input) { return ApiResult.ok(service.topup(token(auth),input)); }
  @PostMapping("/membership/orders") public ApiResult<Map<String,Object>> membership(@RequestHeader(value="Authorization", required=false) String auth,@RequestBody Map<String,Object> input) { return ApiResult.ok(service.membership(token(auth),input)); }

  @PostMapping("/ai/scripts") public ApiResult<Map<String,Object>> aiScript(@RequestHeader(value="Authorization", required=false) String auth,@RequestBody Map<String,Object> input) { return ApiResult.ok(service.aiScript(token(auth),input)); }
  @GetMapping("/ai/scripts") public ApiResult<List<Map<String,Object>>> aiScripts(@RequestHeader(value="Authorization", required=false) String auth) { return ApiResult.ok(service.aiScripts(token(auth))); }
  @GetMapping("/messages/conversations") public ApiResult<List<Map<String,Object>>> conversations() { return ApiResult.ok(service.conversations()); }
  @GetMapping("/messages/{id}") public ApiResult<List<Map<String,Object>>> messages(@PathVariable String id) { return ApiResult.ok(service.messages(id)); }
  @PostMapping("/messages/{id}") public ApiResult<Map<String,Object>> sendMessage(@RequestHeader(value="Authorization", required=false) String auth,@PathVariable String id,@RequestBody Map<String,Object> input) { return ApiResult.ok(service.sendMessage(token(auth),id,input)); }
  @GetMapping("/messages/quota") public ApiResult<Map<String,Object>> quota(@RequestHeader(value="Authorization", required=false) String auth) { return ApiResult.ok(service.quota(token(auth))); }

  @GetMapping("/contracts") public ApiResult<List<Map<String,Object>>> contracts() { return ApiResult.ok(service.contracts()); }
  @PostMapping("/contracts") public ApiResult<Map<String,Object>> createContract(@RequestHeader(value="Authorization", required=false) String auth,@RequestBody Map<String,Object> input) { return ApiResult.ok(auth == null || auth.isBlank() ? service.createContract(input) : service.createAnchorContract(token(auth), input)); }
  @GetMapping("/settlements") public ApiResult<List<Map<String,Object>>> settlements() { return ApiResult.ok(service.settlements()); }
  @PostMapping("/contracts/{id}/settle") public ApiResult<Map<String,Object>> settle(@RequestHeader(value="Authorization", required=false) String auth,@PathVariable String id,@RequestBody Map<String,Object> input) { return ApiResult.ok(auth == null || auth.isBlank() ? service.settle(id,input) : service.settleAnchorContract(token(auth), id, input)); }
  @GetMapping("/invitations") public ApiResult<List<Map<String,Object>>> invitations() { return ApiResult.ok(service.invitations()); }
  @PostMapping("/invitations") public ApiResult<Map<String,Object>> invitation(@RequestBody Map<String,Object> input) { return ApiResult.ok(service.invitation(input)); }

  @GetMapping("/courses") public ApiResult<List<Map<String,Object>>> courses(@RequestParam(required=false) String mode) { return ApiResult.ok(service.courses(mode)); }
  @GetMapping("/courses/enrollments") public ApiResult<List<Map<String,Object>>> enrollments(@RequestHeader(value="Authorization", required=false) String auth) { return ApiResult.ok(service.enrollments(token(auth))); }
  @PostMapping("/courses/{id}/enroll") public ApiResult<Map<String,Object>> enroll(@RequestHeader(value="Authorization", required=false) String auth,@PathVariable String id) { return ApiResult.ok(service.enroll(token(auth),id)); }
  @PostMapping("/courses/enrollments/{id}/exam") public ApiResult<Map<String,Object>> exam(@RequestHeader(value="Authorization", required=false) String auth,@PathVariable String id,@RequestBody Map<String,Object> input) { return ApiResult.ok(service.exam(token(auth),id,input)); }
  @GetMapping("/equipment/products") public ApiResult<List<Map<String,Object>>> products() { return ApiResult.ok(service.products()); }
  @PostMapping("/equipment/products/{id}/orders") public ApiResult<Map<String,Object>> orderProduct(@RequestHeader(value="Authorization", required=false) String auth,@PathVariable String id,@RequestBody(required=false) Map<String,Object> input) { return ApiResult.ok(service.orderProduct(token(auth),id,input == null ? Map.of() : input)); }
  @GetMapping("/equipment/orders") public ApiResult<List<Map<String,Object>>> orders(@RequestHeader(value="Authorization", required=false) String auth) { return ApiResult.ok(service.orders(token(auth))); }

  @GetMapping("/events") public ApiResult<List<Map<String,Object>>> events() { return ApiResult.ok(service.events()); }
  @PostMapping("/events/{id}/register") public ApiResult<Map<String,Object>> eventRegister(@RequestHeader(value="Authorization", required=false) String auth,@PathVariable String id) { return ApiResult.ok(service.registerEvent(token(auth),id)); }
  @GetMapping("/events/{id}/registrations") public ApiResult<List<Map<String,Object>>> eventRegistrations(@PathVariable String id) { return ApiResult.ok(service.eventRegistrations(id)); }
  @PostMapping("/events/registrations/{id}/vote") public ApiResult<Map<String,Object>> vote(@PathVariable String id) { return ApiResult.ok(service.vote(id)); }

  @PostMapping("/cross-border/settlements") public ApiResult<Map<String,Object>> crossBorder(@RequestHeader(value="Authorization", required=false) String auth,@RequestBody Map<String,Object> input) { return ApiResult.ok(service.crossBorder(token(auth),input)); }
  @GetMapping("/cross-border/settlements") public ApiResult<List<Map<String,Object>>> crossBorders(@RequestHeader(value="Authorization", required=false) String auth) { return ApiResult.ok(service.crossBorders(token(auth))); }
  @GetMapping("/eor/providers") public ApiResult<List<Map<String,Object>>> providers(@RequestParam(required=false) String country) { return ApiResult.ok(service.providers(country)); }
  @GetMapping("/eor/requests") public ApiResult<List<Map<String,Object>>> eorRequests() { return ApiResult.ok(service.eorRequests()); }
  @PostMapping("/eor/requests") public ApiResult<Map<String,Object>> eorRequest(@RequestBody Map<String,Object> input) { return ApiResult.ok(service.eorRequest(input)); }

  @GetMapping("/admin/overview") public ApiResult<Map<String,Object>> overview() { return ApiResult.ok(service.adminOverview()); }
  @GetMapping("/admin/export") public ApiResult<Map<String,Object>> export() { return ApiResult.ok(service.adminExport()); }
  @GetMapping("/admin/anchor-cards") public ApiResult<List<Map<String,Object>>> anchorCards() { return ApiResult.ok(service.adminAnchorCards()); }
}
