-- Add product property column to production plan table
ALTER TABLE sys_production_plan
ADD COLUMN product_property INT DEFAULT 3 COMMENT 'Product property: 2=Semi-finished, 3=Finished product';
