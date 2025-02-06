-- Step 1: Add a new column with updated ENUM
ALTER TABLE feedback
ADD COLUMN new_status ENUM('PENDING', 'IN_PROGRESS', 'RESOLVED', 'REJECTED', 'APPROVED') DEFAULT 'PENDING';

-- Step 2: Copy existing data to the new column
UPDATE feedback SET new_status = status;

-- Step 3: Drop the old column
ALTER TABLE feedback DROP COLUMN status;

-- Step 4: Rename the new column to replace the old one
ALTER TABLE feedback CHANGE new_status status ENUM('PENDING', 'IN_PROGRESS', 'RESOLVED', 'REJECTED', 'APPROVED') DEFAULT 'PENDING';
