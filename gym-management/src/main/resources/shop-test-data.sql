-- Du lieu san pham dung de test cua hang. Co the chay lai ma khong tao ban ghi trung.
INSERT INTO shop_products (id, name, category, description, brand, image_url, price, sale_price, stock, active, suitable_goals, required_equipment_code, created_at)
SELECT 31001, 'Whey Protein Vanilla 1 kg', 'SUPPLEMENT', 'Bột whey vị vani, cung cấp 24 g protein mỗi khẩu phần, phù hợp dùng sau buổi tập.', 'GYMPRO Nutrition', 'https://images.unsplash.com/photo-1593095948071-474c5cc2989d?auto=format&fit=crop&w=800&q=80', 799000, 699000, 25, TRUE, 'MUSCLE_GAIN,MAINTENANCE', NULL, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM shop_products WHERE id = 31001);

INSERT INTO shop_products (id, name, category, description, brand, image_url, price, sale_price, stock, active, suitable_goals, required_equipment_code, created_at)
SELECT 31002, 'Creatine Monohydrate 300 g', 'SUPPLEMENT', 'Creatine monohydrate không mùi, 60 khẩu phần, hỗ trợ mục tiêu sức mạnh và sức bền.', 'GYMPRO Nutrition', 'https://images.unsplash.com/photo-1579722821273-0f6c7d44362f?auto=format&fit=crop&w=800&q=80', 399000, NULL, 30, TRUE, 'MUSCLE_GAIN,ENDURANCE', NULL, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM shop_products WHERE id = 31002);

INSERT INTO shop_products (id, name, category, description, brand, image_url, price, sale_price, stock, active, suitable_goals, required_equipment_code, created_at)
SELECT 31003, 'Protein Bar Chocolate', 'SUPPLEMENT', 'Thanh protein vị chocolate chứa 20 g protein, ít đường, tiện dùng cho bữa phụ.', 'Fit Snack', 'https://images.unsplash.com/photo-1571748982800-fa51082c2224?auto=format&fit=crop&w=800&q=80', 45000, 39000, 60, TRUE, 'WEIGHT_LOSS,MAINTENANCE', NULL, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM shop_products WHERE id = 31003);

INSERT INTO shop_products (id, name, category, description, brand, image_url, price, sale_price, stock, active, suitable_goals, required_equipment_code, created_at)
SELECT 31004, 'Ức gà áp chảo 300 g', 'FOOD', 'Ức gà áp chảo đóng hộp, giàu protein, dùng trong ngày và bảo quản lạnh.', 'GYMPRO Kitchen', 'https://images.unsplash.com/photo-1532550907401-a500c9a57435?auto=format&fit=crop&w=800&q=80', 79000, NULL, 20, TRUE, 'MUSCLE_GAIN,WEIGHT_LOSS', NULL, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM shop_products WHERE id = 31004);

INSERT INTO shop_products (id, name, category, description, brand, image_url, price, sale_price, stock, active, suitable_goals, required_equipment_code, created_at)
SELECT 31005, 'Combo 5 bữa kiểm soát calo', 'FOOD', 'Combo năm bữa ăn đóng gói, mỗi phần có thông tin năng lượng và thành phần dinh dưỡng.', 'GYMPRO Kitchen', 'https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=800&q=80', 425000, 389000, 15, TRUE, 'WEIGHT_LOSS,MAINTENANCE', NULL, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM shop_products WHERE id = 31005);

INSERT INTO shop_products (id, name, category, description, brand, image_url, price, sale_price, stock, active, suitable_goals, required_equipment_code, created_at)
SELECT 31006, 'Yến mạch nguyên hạt 500 g', 'FOOD', 'Yến mạch nguyên hạt giàu chất xơ, phù hợp cho bữa sáng hoặc bữa phụ trước tập.', 'Healthy Farm', 'https://images.unsplash.com/photo-1517673400267-0251440c45dc?auto=format&fit=crop&w=800&q=80', 69000, NULL, 35, TRUE, 'WEIGHT_LOSS,ENDURANCE,MAINTENANCE', NULL, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM shop_products WHERE id = 31006);

INSERT INTO shop_products (id, name, category, description, brand, image_url, price, sale_price, stock, active, suitable_goals, required_equipment_code, created_at)
SELECT 31007, 'Bộ dây kháng lực 5 mức', 'EQUIPMENT', 'Bộ năm dây với các mức kháng lực khác nhau, kèm túi đựng và hướng dẫn cơ bản.', 'GYMPRO Gear', 'https://images.unsplash.com/photo-1598289431512-b97b0917affc?auto=format&fit=crop&w=800&q=80', 249000, 219000, 25, TRUE, 'MUSCLE_GAIN,WEIGHT_LOSS,ENDURANCE', 'RESISTANCE_BAND', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM shop_products WHERE id = 31007);

INSERT INTO shop_products (id, name, category, description, brand, image_url, price, sale_price, stock, active, suitable_goals, required_equipment_code, created_at)
SELECT 31008, 'Thảm tập chống trượt 8 mm', 'EQUIPMENT', 'Thảm TPE dày 8 mm, bề mặt chống trượt, phù hợp các bài tập sàn và giãn cơ.', 'GYMPRO Gear', 'https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?auto=format&fit=crop&w=800&q=80', 299000, NULL, 18, TRUE, 'WEIGHT_LOSS,ENDURANCE,MAINTENANCE', 'MAT', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM shop_products WHERE id = 31008);

INSERT INTO shop_products (id, name, category, description, brand, image_url, price, sale_price, stock, active, suitable_goals, required_equipment_code, created_at)
SELECT 31009, 'Cặp tạ đơn bọc cao su 5 kg', 'EQUIPMENT', 'Hai tạ đơn 5 kg bọc cao su, tay cầm chống trượt, tổng khối lượng 10 kg.', 'GYMPRO Gear', 'https://images.unsplash.com/photo-1586401100295-7a8096fd231a?auto=format&fit=crop&w=800&q=80', 499000, 459000, 12, TRUE, 'MUSCLE_GAIN,MAINTENANCE', 'DUMBBELL', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM shop_products WHERE id = 31009);

-- San pham het hang de test trang thai nut mua.
INSERT INTO shop_products (id, name, category, description, brand, image_url, price, sale_price, stock, active, suitable_goals, required_equipment_code, created_at)
SELECT 31010, 'Găng tay tập gym Pro', 'EQUIPMENT', 'Găng tay thoáng khí có đệm lòng bàn tay, hỗ trợ cầm nắm khi tập tạ.', 'GYMPRO Gear', 'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?auto=format&fit=crop&w=800&q=80', 159000, NULL, 0, TRUE, 'MUSCLE_GAIN,MAINTENANCE', NULL, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM shop_products WHERE id = 31010);

ALTER TABLE shop_products ALTER COLUMN id RESTART WITH 1000000;
