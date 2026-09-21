-- CHI DUNG TREN DATABASE BAN SAO DANH CHO DIAGRAM, schema dbo.
-- Can co san 43 bang goc. Them 16 FK de the hien lien ket logic.
-- Khong dung tren database ung dung: FK moi co the anh huong xoa/sua du lieu.
-- Neu co du lieu mo coi, SQL Server se bao loi va transaction se rollback.
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_key_columns
    WHERE parent_object_id = OBJECT_ID(N'dbo.customer_addresses')
      AND parent_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.customer_addresses'), N'user_id', 'ColumnId')
      AND referenced_object_id = OBJECT_ID(N'dbo.users')
      AND referenced_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.users'), N'id', 'ColumnId')
)
    ALTER TABLE [dbo].[customer_addresses] WITH CHECK ADD CONSTRAINT [FK_diagram_customer_addresses_user_id]
    FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([id]);

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_key_columns
    WHERE parent_object_id = OBJECT_ID(N'dbo.customer_product_states')
      AND parent_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.customer_product_states'), N'user_id', 'ColumnId')
      AND referenced_object_id = OBJECT_ID(N'dbo.users')
      AND referenced_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.users'), N'id', 'ColumnId')
)
    ALTER TABLE [dbo].[customer_product_states] WITH CHECK ADD CONSTRAINT [FK_diagram_customer_product_states_user_id]
    FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([id]);

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_key_columns
    WHERE parent_object_id = OBJECT_ID(N'dbo.customer_product_states')
      AND parent_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.customer_product_states'), N'product_id', 'ColumnId')
      AND referenced_object_id = OBJECT_ID(N'dbo.shop_products')
      AND referenced_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.shop_products'), N'id', 'ColumnId')
)
    ALTER TABLE [dbo].[customer_product_states] WITH CHECK ADD CONSTRAINT [FK_diagram_customer_product_states_product_id]
    FOREIGN KEY ([product_id]) REFERENCES [dbo].[shop_products] ([id]);

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_key_columns
    WHERE parent_object_id = OBJECT_ID(N'dbo.user_cosmetic_ownership')
      AND parent_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.user_cosmetic_ownership'), N'user_id', 'ColumnId')
      AND referenced_object_id = OBJECT_ID(N'dbo.users')
      AND referenced_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.users'), N'id', 'ColumnId')
)
    ALTER TABLE [dbo].[user_cosmetic_ownership] WITH CHECK ADD CONSTRAINT [FK_diagram_user_cosmetic_ownership_user_id]
    FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([id]);

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_key_columns
    WHERE parent_object_id = OBJECT_ID(N'dbo.shop_order_events')
      AND parent_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.shop_order_events'), N'order_id', 'ColumnId')
      AND referenced_object_id = OBJECT_ID(N'dbo.shop_orders')
      AND referenced_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.shop_orders'), N'id', 'ColumnId')
)
    ALTER TABLE [dbo].[shop_order_events] WITH CHECK ADD CONSTRAINT [FK_diagram_shop_order_events_order_id]
    FOREIGN KEY ([order_id]) REFERENCES [dbo].[shop_orders] ([id]);

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_key_columns
    WHERE parent_object_id = OBJECT_ID(N'dbo.shop_inventory_movements')
      AND parent_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.shop_inventory_movements'), N'product_id', 'ColumnId')
      AND referenced_object_id = OBJECT_ID(N'dbo.shop_products')
      AND referenced_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.shop_products'), N'id', 'ColumnId')
)
    ALTER TABLE [dbo].[shop_inventory_movements] WITH CHECK ADD CONSTRAINT [FK_diagram_shop_inventory_movements_product_id]
    FOREIGN KEY ([product_id]) REFERENCES [dbo].[shop_products] ([id]);

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_key_columns
    WHERE parent_object_id = OBJECT_ID(N'dbo.shop_inventory_movements')
      AND parent_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.shop_inventory_movements'), N'variant_id', 'ColumnId')
      AND referenced_object_id = OBJECT_ID(N'dbo.product_variants')
      AND referenced_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.product_variants'), N'id', 'ColumnId')
)
    ALTER TABLE [dbo].[shop_inventory_movements] WITH CHECK ADD CONSTRAINT [FK_diagram_shop_inventory_movements_variant_id]
    FOREIGN KEY ([variant_id]) REFERENCES [dbo].[product_variants] ([id]);

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_key_columns
    WHERE parent_object_id = OBJECT_ID(N'dbo.shop_inventory_movements')
      AND parent_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.shop_inventory_movements'), N'order_id', 'ColumnId')
      AND referenced_object_id = OBJECT_ID(N'dbo.shop_orders')
      AND referenced_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.shop_orders'), N'id', 'ColumnId')
)
    ALTER TABLE [dbo].[shop_inventory_movements] WITH CHECK ADD CONSTRAINT [FK_diagram_shop_inventory_movements_order_id]
    FOREIGN KEY ([order_id]) REFERENCES [dbo].[shop_orders] ([id]);

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_key_columns
    WHERE parent_object_id = OBJECT_ID(N'dbo.shop_cart_items')
      AND parent_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.shop_cart_items'), N'variant_id', 'ColumnId')
      AND referenced_object_id = OBJECT_ID(N'dbo.product_variants')
      AND referenced_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.product_variants'), N'id', 'ColumnId')
)
    ALTER TABLE [dbo].[shop_cart_items] WITH CHECK ADD CONSTRAINT [FK_diagram_shop_cart_items_variant_id]
    FOREIGN KEY ([variant_id]) REFERENCES [dbo].[product_variants] ([id]);

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_key_columns
    WHERE parent_object_id = OBJECT_ID(N'dbo.shop_order_items')
      AND parent_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.shop_order_items'), N'product_id', 'ColumnId')
      AND referenced_object_id = OBJECT_ID(N'dbo.shop_products')
      AND referenced_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.shop_products'), N'id', 'ColumnId')
)
    ALTER TABLE [dbo].[shop_order_items] WITH CHECK ADD CONSTRAINT [FK_diagram_shop_order_items_product_id]
    FOREIGN KEY ([product_id]) REFERENCES [dbo].[shop_products] ([id]);

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_key_columns
    WHERE parent_object_id = OBJECT_ID(N'dbo.shop_order_items')
      AND parent_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.shop_order_items'), N'variant_id', 'ColumnId')
      AND referenced_object_id = OBJECT_ID(N'dbo.product_variants')
      AND referenced_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.product_variants'), N'id', 'ColumnId')
)
    ALTER TABLE [dbo].[shop_order_items] WITH CHECK ADD CONSTRAINT [FK_diagram_shop_order_items_variant_id]
    FOREIGN KEY ([variant_id]) REFERENCES [dbo].[product_variants] ([id]);

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_key_columns
    WHERE parent_object_id = OBJECT_ID(N'dbo.shop_orders')
      AND parent_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.shop_orders'), N'created_by_staff_id', 'ColumnId')
      AND referenced_object_id = OBJECT_ID(N'dbo.users')
      AND referenced_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.users'), N'id', 'ColumnId')
)
    ALTER TABLE [dbo].[shop_orders] WITH CHECK ADD CONSTRAINT [FK_diagram_shop_orders_created_by_staff_id]
    FOREIGN KEY ([created_by_staff_id]) REFERENCES [dbo].[users] ([id]);

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_key_columns
    WHERE parent_object_id = OBJECT_ID(N'dbo.shop_orders')
      AND parent_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.shop_orders'), N'voucher_code', 'ColumnId')
      AND referenced_object_id = OBJECT_ID(N'dbo.vouchers')
      AND referenced_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.vouchers'), N'code', 'ColumnId')
)
    ALTER TABLE [dbo].[shop_orders] WITH CHECK ADD CONSTRAINT [FK_diagram_shop_orders_voucher_code]
    FOREIGN KEY ([voucher_code]) REFERENCES [dbo].[vouchers] ([code]);

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_key_columns
    WHERE parent_object_id = OBJECT_ID(N'dbo.voucher_scope_products')
      AND parent_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.voucher_scope_products'), N'product_id', 'ColumnId')
      AND referenced_object_id = OBJECT_ID(N'dbo.shop_products')
      AND referenced_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.shop_products'), N'id', 'ColumnId')
)
    ALTER TABLE [dbo].[voucher_scope_products] WITH CHECK ADD CONSTRAINT [FK_diagram_voucher_scope_products_product_id]
    FOREIGN KEY ([product_id]) REFERENCES [dbo].[shop_products] ([id]);

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_key_columns
    WHERE parent_object_id = OBJECT_ID(N'dbo.workout_plans')
      AND parent_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.workout_plans'), N'original_plan_id', 'ColumnId')
      AND referenced_object_id = OBJECT_ID(N'dbo.workout_plans')
      AND referenced_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.workout_plans'), N'id', 'ColumnId')
)
    ALTER TABLE [dbo].[workout_plans] WITH CHECK ADD CONSTRAINT [FK_diagram_workout_plans_original_plan_id]
    FOREIGN KEY ([original_plan_id]) REFERENCES [dbo].[workout_plans] ([id]);

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_key_columns
    WHERE parent_object_id = OBJECT_ID(N'dbo.workout_plan_exercises')
      AND parent_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.workout_plan_exercises'), N'last_low_adjustment_log_id', 'ColumnId')
      AND referenced_object_id = OBJECT_ID(N'dbo.session_exercise_logs')
      AND referenced_column_id = COLUMNPROPERTY(OBJECT_ID(N'dbo.session_exercise_logs'), N'id', 'ColumnId')
)
    ALTER TABLE [dbo].[workout_plan_exercises] WITH CHECK ADD CONSTRAINT [FK_diagram_workout_plan_exercises_last_low_adjustment_log_id]
    FOREIGN KEY ([last_low_adjustment_log_id]) REFERENCES [dbo].[session_exercise_logs] ([id]);
COMMIT TRANSACTION;
