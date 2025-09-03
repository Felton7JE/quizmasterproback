-- Migration script to convert Category enum to Category entity
-- Execute this script AFTER running the application once to create the new tables

-- 1. Create categories table (will be created automatically by JPA)
-- 2. Insert default categories
INSERT INTO categories (name, display_name, description, is_active, created_at) VALUES
('MATH', 'Matemática', 'Questões de matemática', true, NOW()),
('PORTUGUESE', 'Português', 'Questões de língua portuguesa', true, NOW()),
('HISTORY', 'História', 'Questões de história', true, NOW()),
('GEOGRAPHY', 'Geografia', 'Questões de geografia', true, NOW()),
('SCIENCE', 'Ciências', 'Questões de ciências', true, NOW()),
('ENGLISH', 'Inglês', 'Questões de inglês', true, NOW()),
('MIXED', 'Misto', 'Questões mistas de várias categorias', true, NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 3. Create room_categories junction table (will be created automatically by JPA)

-- 4. Migrate existing questions
-- Add category_id column if not exists
ALTER TABLE questions ADD COLUMN IF NOT EXISTS category_id BIGINT;

-- Update questions with category_id based on existing category enum
UPDATE questions q 
JOIN categories c ON q.category = c.name 
SET q.category_id = c.id;

-- Add foreign key constraint
ALTER TABLE questions ADD CONSTRAINT fk_question_category 
FOREIGN KEY (category_id) REFERENCES categories(id);

-- 5. Migrate room categories (if rooms table uses categories)
-- This will need to be done programmatically as it involves converting ElementCollection to ManyToMany

-- 6. Migrate room players categories
-- Add columns if not exist
ALTER TABLE room_players ADD COLUMN IF NOT EXISTS preferred_category_id BIGINT;
ALTER TABLE room_players ADD COLUMN IF NOT EXISTS assigned_category_id BIGINT;

-- Update room players with category_ids based on existing enum values
UPDATE room_players rp 
JOIN categories c ON rp.preferred_category = c.name 
SET rp.preferred_category_id = c.id;

UPDATE room_players rp 
JOIN categories c ON rp.assigned_category = c.name 
SET rp.assigned_category_id = c.id;

-- Add foreign key constraints
ALTER TABLE room_players ADD CONSTRAINT fk_room_player_preferred_category 
FOREIGN KEY (preferred_category_id) REFERENCES categories(id);

ALTER TABLE room_players ADD CONSTRAINT fk_room_player_assigned_category 
FOREIGN KEY (assigned_category_id) REFERENCES categories(id);

-- 7. Drop old enum columns (DO THIS LAST, AFTER VERIFYING DATA MIGRATION)
-- ALTER TABLE questions DROP COLUMN category;
-- ALTER TABLE room_players DROP COLUMN preferred_category;
-- ALTER TABLE room_players DROP COLUMN assigned_category;
