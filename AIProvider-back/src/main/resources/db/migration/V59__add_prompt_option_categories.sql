CREATE TABLE c_PromptOptionCategories (
  Category VARCHAR(40) NOT NULL,
  Name VARCHAR(100) NOT NULL,
  SortOrder INT NOT NULL,
  AllowMultiple BOOLEAN NOT NULL DEFAULT TRUE,
  Enabled BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (Category),
  KEY IX_PromptOptionCategories_SortOrder (SortOrder)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO c_PromptOptionCategories(Category, Name, SortOrder, AllowMultiple, Enabled) VALUES
('Appearance', '外貌', 1, TRUE, TRUE),
('Body', '身体', 2, TRUE, TRUE),
('Feature', '特征', 3, TRUE, TRUE),
('Character', '人物', 4, TRUE, TRUE),
('Special', '特殊', 5, TRUE, TRUE),
('Clothing', '服装', 6, TRUE, TRUE),
('Sex Act', '性行为', 7, TRUE, TRUE),
('Sex Position', '性姿势', 8, TRUE, TRUE),
('Bondage', '束缚', 9, TRUE, TRUE),
('Artist', '画师', 10, TRUE, TRUE),
('Pose', '姿势', 11, FALSE, TRUE),
('Expression', '表情', 12, FALSE, TRUE),
('Camera', '镜头', 13, FALSE, TRUE),
('Hair', '头发', 14, TRUE, TRUE),
('Relationship', '人物关系', 15, TRUE, TRUE),
('Action', '行为', 16, TRUE, TRUE),
('Eyes', '眼睛', 17, TRUE, TRUE),
('Background', '背景', 18, TRUE, TRUE),
('Lighting', '光照', 19, TRUE, TRUE),
('Composition', '构图', 20, TRUE, TRUE),
('Style', '风格', 21, TRUE, TRUE),
('Quality', '画质词', 22, TRUE, TRUE);

ALTER TABLE c_PromptOptions
  ADD CONSTRAINT FK_PromptOptions_Category
  FOREIGN KEY (Category) REFERENCES c_PromptOptionCategories(Category);
