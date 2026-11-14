INSERT INTO inventory_items (id, sku, name, category, quantity, minimum_quantity, reorder_quantity, status) VALUES
  (1, 'CHALK-WHT', 'White chalk boxes', 'CONSUMABLE', 4, 20, 50, 'LOW_STOCK'),
  (2, 'PAPER-A4', 'A4 paper reams', 'CONSUMABLE', 32, 15, 40, 'OPERATIONAL'),
  (3, 'LAB-BEAKER', 'Glass beakers', 'LAB_EQUIPMENT', 8, 12, 24, 'LOW_STOCK'),
  (4, 'LAB-MICROSCOPE', 'Microscopes', 'LAB_EQUIPMENT', 6, 3, 2, 'NEEDS_REPAIR'),
  (5, 'SPORT-BALL', 'Football stock', 'SPORTS_GEAR', 3, 10, 20, 'LOW_STOCK'),
  (6, 'SPORT-MAT', 'Exercise mats', 'SPORTS_GEAR', 24, 10, 15, 'OPERATIONAL'),
  (7, 'IT-PROJECTOR', 'Classroom projectors', 'IT_HARDWARE', 2, 4, 4, 'DAMAGED'),
  (8, 'IT-MOUSE', 'Computer mice', 'IT_HARDWARE', 30, 12, 25, 'OPERATIONAL');
ALTER TABLE inventory_items ALTER COLUMN id RESTART WITH 100;
