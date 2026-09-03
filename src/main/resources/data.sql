MERGE INTO app_user (id, role, nickname, avatar, phone, verified, city, categories, intro, experience_years, card_status, card_data, created_at) KEY(id)
VALUES ('u_demo', 'anchor', '米粒', '', '138****6608', TRUE, '杭州', '["live-commerce","beauty"]', '3 年带货直播经验，擅长美妆与女装类目。', 3, 'COMPLETE', '{"stageName":"米粒","categories":["live-commerce","beauty"],"city":"杭州","intro":"3 年带货直播经验，擅长美妆与女装类目。","experienceYears":3,"expectedSalary":"10-30K/月","availableTime":"工作日 18:00 后，周末可排班"}', 1788336000000);

MERGE INTO app_user (id, role, nickname, avatar, phone, verified, city, categories, intro, experience_years, card_status, card_data, created_at) KEY(id)
VALUES ('u_merchant_demo', 'merchant', '星耀文化传媒', '', '139****5200', TRUE, '杭州', '[]', '杭州星耀文化传媒招聘中心', 0, 'INCOMPLETE', '{}', 1788336000000);

MERGE INTO app_user (id, role, nickname, avatar, phone, verified, city, categories, intro, experience_years, card_status, card_data, created_at) KEY(id)
VALUES ('u_anchor_aya', 'anchor', '安安', '', '138****1036', TRUE, '上海', '["美妆","家清"]', '镜头表现自然，擅长美妆护肤与家清产品讲解，注重真实体验和转化。', 2, 'COMPLETE', '{"stageName":"安安","categories":["美妆","家清"],"city":"上海","intro":"镜头表现自然，擅长美妆护肤与家清产品讲解，注重真实体验和转化。","experienceYears":2,"expectedSalary":"100-200元/小时","availableTime":"工作日及周末可排班","age":23,"gender":"女","height":"166cm","weight":"47kg","shoeSize":"37码","education":"本科及以上","expectedCities":["上海","杭州"],"acceptShift":false,"experienceCategory":"美妆护肤 / 个护家清","accountName":"合****","peakGmv":"30万","liveYears":2,"advantage":"亲和力强、学习能力快，能够快速理解产品卖点","groupName":"上海主播招聘群","groupDescription":"免费招主播 · 免费进群"}', 1788335900000);

MERGE INTO app_user (id, role, nickname, avatar, phone, verified, city, categories, intro, experience_years, card_status, card_data, created_at) KEY(id)
VALUES ('u_anchor_yang', 'anchor', '杨一', '', '138****2718', TRUE, '杭州', '["服饰","保健"]', '男装与健康品类主播，控场稳定，能够独立完成脚本梳理和直播复盘。', 2, 'COMPLETE', '{"stageName":"杨一","categories":["服饰","保健"],"city":"杭州","intro":"男装与健康品类主播，控场稳定，能够独立完成脚本梳理和直播复盘。","experienceYears":2,"expectedSalary":"10-20K/月","availableTime":"全职，可排班","age":26,"gender":"男","height":"182cm","weight":"68kg","shoeSize":"43码","education":"大专及以上","expectedCities":["杭州"],"acceptShift":true,"experienceCategory":"服饰 / 家电 / 保健","accountName":"播****","peakGmv":"42万","liveYears":2,"advantage":"亲和力强、学习能力快，产品拆解和现场应变能力好","groupName":"杭州主播招聘群","groupDescription":"优质岗位 · 免费进群"}', 1788335800000);

MERGE INTO app_user (id, role, nickname, avatar, phone, verified, city, categories, intro, experience_years, card_status, card_data, created_at) KEY(id)
VALUES ('u_anchor_jia', 'anchor', '加加', '', '138****4482', TRUE, '厦门', '["食品","本地生活"]', '擅长食品和本地生活直播，表达有感染力，熟悉短视频预热与直播承接。', 2, 'COMPLETE', '{"stageName":"加加","categories":["食品","本地生活"],"city":"厦门","intro":"擅长食品和本地生活直播，表达有感染力，熟悉短视频预热与直播承接。","experienceYears":2,"expectedSalary":"100-200元/小时","availableTime":"档期可沟通","age":21,"gender":"女","height":"163cm","weight":"48kg","shoeSize":"37码","education":"大专及以上","expectedCities":["厦门","泉州"],"acceptShift":false,"experienceCategory":"食品 / 本地生活","accountName":"加****","peakGmv":"18万","liveYears":2,"advantage":"表达自然，擅长现场互动和生活化卖点呈现","groupName":"厦门主播招聘群","groupDescription":"本地优选 · 免费进群"}', 1788335700000);

MERGE INTO user_wallet (user_id, card_balance, member_level, ai_quota, updated_at) KEY(user_id)
VALUES ('u_demo', 8, 'PRO', 20, 1788336000000);

MERGE INTO user_wallet (user_id, card_balance, member_level, ai_quota, updated_at) KEY(user_id)
VALUES ('u_merchant_demo', 0, 'BUSINESS', 0, 1788336000000);

MERGE INTO message_quota (user_id, remaining_count, total_count) KEY(user_id)
VALUES ('u_demo', 12, 20);

MERGE INTO message_quota (user_id, remaining_count, total_count) KEY(user_id)
VALUES ('u_merchant_demo', 0, 0);

MERGE INTO job_notice (id, title, job_type, category, salary_min, salary_max, salary_unit, salary_display, city, address, distance_km, longitude, latitude, duties, requirements, tags, publisher_id, publisher_name, publisher_avatar, publisher_real_name, publisher_enterprise, urgent, published_at, view_count, status, apply_count) KEY(id)
VALUES ('n_1001', '带货主播', 'full-time', 'live-commerce', 10000, 30000, 'month', '10-30K/月', '杭州', '杭州市余杭区文一西路969号', 1.2, 120.026208, 30.279135, '["负责女装直播讲解与转化","配合运营完成复盘"]', '["有直播经验优先","表达流畅"]', '["高提成","五险一金","包住"]', 'p_2001', '杭州星耀文化传媒有限公司', '', TRUE, TRUE, TRUE, 1788334200000, 412, 'PUBLISHED', 28);

MERGE INTO job_notice (id, title, job_type, category, salary_min, salary_max, salary_unit, salary_display, city, address, distance_km, longitude, latitude, duties, requirements, tags, publisher_id, publisher_name, publisher_avatar, publisher_real_name, publisher_enterprise, urgent, published_at, view_count, status, apply_count) KEY(id)
VALUES ('n_1002', '娱乐主播', 'part-time', 'entertainment', 300, 500, 'session', '300-500/场', '上海', '上海市静安区南京西路1266号', 3.6, 121.444620, 31.223000, '["才艺与聊天直播","维护直播间互动"]', '["声音条件好","每天稳定开播"]', '["日结","时间自由","可远程"]', 'p_2002', '上海耀阳网络科技有限公司', '', TRUE, FALSE, TRUE, 1788328800000, 256, 'PUBLISHED', 19);

MERGE INTO job_notice (id, title, job_type, category, salary_min, salary_max, salary_unit, salary_display, city, address, distance_km, longitude, latitude, duties, requirements, tags, publisher_id, publisher_name, publisher_avatar, publisher_real_name, publisher_enterprise, urgent, published_at, view_count, status, apply_count) KEY(id)
VALUES ('n_1003', '游戏主播', 'part-time', 'game', 200, 400, 'day', '200-400/天', '成都', '成都市高新区天府三街199号', 5.8, 104.064760, 30.570200, '["直播热门手游","配合赛事活动"]', '["热门游戏高段位","声音有辨识度"]', '["日结","包宿","设备补贴"]', 'p_2003', '成都锋游文化传播有限公司', '', TRUE, TRUE, FALSE, 1788314400000, 189, 'PUBLISHED', 11);

MERGE INTO job_notice (id, title, job_type, category, salary_min, salary_max, salary_unit, salary_display, city, address, distance_km, longitude, latitude, duties, requirements, tags, publisher_id, publisher_name, publisher_avatar, publisher_real_name, publisher_enterprise, urgent, published_at, view_count, status, apply_count) KEY(id)
VALUES ('n_1004', '户外探店主播', 'full-time', 'outdoor', 8000, 20000, 'month', '8-20K/月', '广州', '广州市天河区珠江新城华夏路10号', 8.3, 113.324460, 23.118460, '["户外探店与旅拍","策划直播选题"]', '["性格外向","可适应户外直播"]', '["交通补贴","底薪+提成"]', 'p_2004', '广州漫游文化传媒有限公司', '', TRUE, TRUE, FALSE, 1788292800000, 204, 'PUBLISHED', 16);

MERGE INTO job_notice (id, title, job_type, category, salary_min, salary_max, salary_unit, salary_display, city, address, distance_km, longitude, latitude, duties, requirements, tags, publisher_id, publisher_name, publisher_avatar, publisher_real_name, publisher_enterprise, urgent, published_at, view_count, status, apply_count) KEY(id)
VALUES ('n_1005', '聊天主播', 'part-time', 'talk', 150, 300, 'session', '150-300/场', '武汉', '武汉市江汉区解放大道688号', 12.5, 114.270000, 30.580000, '["语音与视频互动","维护用户关系"]', '["善于沟通","情绪稳定"]', '["日结","可远程","新手可做"]', 'p_2005', '武汉暖音网络科技有限公司', '', TRUE, FALSE, FALSE, 1788249600000, 167, 'PUBLISHED', 8);

MERGE INTO job_notice (id, title, job_type, category, salary_min, salary_max, salary_unit, salary_display, city, address, distance_km, longitude, latitude, duties, requirements, tags, publisher_id, publisher_name, publisher_avatar, publisher_real_name, publisher_enterprise, urgent, published_at, view_count, status, apply_count) KEY(id)
VALUES ('n_1006', '美妆带货主播', 'full-time', 'live-commerce', 12000, 35000, 'month', '12-35K/月', '深圳', '深圳市南山区科技园高新南一道', 15.1, 113.953600, 22.539500, '["美妆护肤直播带货","主导控场与转化"]', '["一年以上美妆经验","镜头感强"]', '["高提成","五险一金","包住"]', 'p_2006', '深圳美创直播基地', '', TRUE, TRUE, TRUE, 1788335100000, 528, 'PUBLISHED', 35);

MERGE INTO job_notice (id, title, job_type, category, salary_min, salary_max, salary_unit, salary_display, city, address, distance_km, longitude, latitude, duties, requirements, tags, publisher_id, publisher_name, publisher_avatar, publisher_real_name, publisher_enterprise, urgent, published_at, view_count, status, apply_count) KEY(id)
VALUES ('n_demo_merchant_1', '带货主播', 'full-time', 'live-commerce', 7000, 15000, 'month', '7-15K/月', '杭州', '杭州市余杭区文一西路969号', 1.2, 120.026208, 30.279135, '["负责女装直播讲解与转化","配合运营完成复盘"]', '["有直播经验优先","表达流畅"]', '["高提成","五险一金"]', 'u_merchant_demo', '星耀文化传媒', '', TRUE, TRUE, TRUE, 1788336000000, 328, 'DRAFT', 46);
MERGE INTO job_notice (id, title, job_type, category, salary_min, salary_max, salary_unit, salary_display, city, address, distance_km, longitude, latitude, duties, requirements, tags, publisher_id, publisher_name, publisher_avatar, publisher_real_name, publisher_enterprise, urgent, published_at, view_count, status, apply_count) KEY(id)
VALUES ('n_demo_merchant_2', '娱乐主播', 'part-time', 'entertainment', 300, 500, 'session', '300-500/场', '上海', '上海市静安区南京西路1266号', 3.6, 121.444620, 31.223000, '["才艺与聊天直播","维护直播间互动"]', '["声音条件好","每天稳定开播"]', '["日结","时间自由"]', 'u_merchant_demo', '星耀文化传媒', '', TRUE, TRUE, TRUE, 1788335800000, 156, 'PENDING', 16);
MERGE INTO job_notice (id, title, job_type, category, salary_min, salary_max, salary_unit, salary_display, city, address, distance_km, longitude, latitude, duties, requirements, tags, publisher_id, publisher_name, publisher_avatar, publisher_real_name, publisher_enterprise, urgent, published_at, view_count, status, apply_count) KEY(id)
VALUES ('n_demo_merchant_3', '游戏主播', 'part-time', 'game', 200, 400, 'day', '200-400/天', '成都', '成都市高新区天府三街199号', 5.8, 104.064760, 30.570200, '["直播热门手游","配合赛事活动"]', '["热门游戏高段位","声音有辨识度"]', '["日结","设备补贴"]', 'u_merchant_demo', '星耀文化传媒', '', TRUE, TRUE, FALSE, 1788335600000, 89, 'PUBLISHED', 34);
MERGE INTO job_notice (id, title, job_type, category, salary_min, salary_max, salary_unit, salary_display, city, address, distance_km, longitude, latitude, duties, requirements, tags, publisher_id, publisher_name, publisher_avatar, publisher_real_name, publisher_enterprise, urgent, published_at, view_count, status, apply_count) KEY(id)
VALUES ('n_demo_merchant_4', '户外主播', 'full-time', 'outdoor', 8000, 20000, 'month', '8-20K/月', '广州', '广州市天河区珠江新城华夏路10号', 8.3, 113.324460, 23.118460, '["户外探店与旅拍","策划直播选题"]', '["性格外向","可适应户外直播"]', '["交通补贴","底薪+提成"]', 'u_merchant_demo', '星耀文化传媒', '', TRUE, FALSE, FALSE, 1788335400000, 42, 'REJECTED', 0);

MERGE INTO conversation (id, role, name, avatar, last_message, last_time, unread) KEY(id)
VALUES ('c_1001', 'merchant', '杭州星耀招聘官', '', '你的资料很匹配，方便聊聊吗？', 1788335700000, 2);
MERGE INTO conversation (id, role, name, avatar, last_message, last_time, unread) KEY(id)
VALUES ('c_1002', 'system', '播聘认证中心', '', '你的实名认证已经通过', 1788249600000, 0);

MERGE INTO chat_message (id, conversation_id, content, message_type, from_me, created_at) KEY(id)
VALUES ('m_1001', 'c_1001', '您好，我对这个岗位很感兴趣', 'text', TRUE, 1788332100000);
MERGE INTO chat_message (id, conversation_id, content, message_type, from_me, created_at) KEY(id)
VALUES ('m_1002', 'c_1001', '你的资料很匹配，方便聊聊吗？', 'text', FALSE, 1788335700000);

MERGE INTO employment_contract (id, anchor_name, company, job_title, amount, status, created_at) KEY(id)
VALUES ('ct_1001', '米粒', '杭州星耀文化传媒', '女装带货主播', 18000, 'ACTIVE', 1788249600000);

MERGE INTO paid_invitation (id, company, anchor_name, job_title, fee, status, created_at) KEY(id)
VALUES ('iv_1001', '杭州星耀文化传媒', '米粒', '女装带货主播', 29.90, 'PENDING', 1788332400000);

MERGE INTO course (id, name, mode, city, starts_at, capacity, enrolled, price, status) KEY(id)
VALUES ('co_online_1', '高转化直播话术训练营', 'ONLINE', '线上', 1788940800000, 300, 86, 199, 'OPEN');
MERGE INTO course (id, name, mode, city, starts_at, capacity, enrolled, price, status) KEY(id)
VALUES ('co_offline_1', '杭州主播镜头表现力实训', 'OFFLINE', '杭州', 1789545600000, 30, 12, 699, 'OPEN');

MERGE INTO equipment_product (id, name, price, group_price, stock, participants, status) KEY(id)
VALUES ('eq_1001', '专业补光灯直播套装', 599, 399, 120, 38, 'ON_SALE');
MERGE INTO equipment_product (id, name, price, group_price, stock, participants, status) KEY(id)
VALUES ('eq_1002', '无线领夹麦克风双人版', 429, 299, 80, 21, 'ON_SALE');

MERGE INTO annual_event (id, name, city, event_date, status) KEY(id)
VALUES ('ev_2026', '2026 播聘年度主播盛典', '杭州', 1797436800000, 'REGISTRATION');

MERGE INTO eor_provider (id, country, name, currencies, service_fee, rating, status) KEY(id)
VALUES ('eor_sg_1', '新加坡', 'Lion City EOR Services', 'SGD,USD,CNY', 0.0650, 4.80, 'ACTIVE');
MERGE INTO eor_provider (id, country, name, currencies, service_fee, rating, status) KEY(id)
VALUES ('eor_my_1', '马来西亚', 'MY Workforce Partner', 'MYR,USD,CNY', 0.0580, 4.70, 'ACTIVE');
MERGE INTO eor_provider (id, country, name, currencies, service_fee, rating, status) KEY(id)
VALUES ('eor_th_1', '泰国', 'Bangkok Employment Hub', 'THB,USD,CNY', 0.0620, 4.60, 'ACTIVE');
