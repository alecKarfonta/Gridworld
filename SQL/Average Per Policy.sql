 CREATE VIEW `Average Per World` AS 
 
 SELECT 
   `World Type` AS `World Type`,
   `Steps` AS `Steps`,
   `Initial E` AS `Initial E`,
   `Learning Rate` AS `Learning Rate`,
   AVG(`Best Found Path`) AS `AVG(``Best Found Path``)` 
 FROM `GridworldResults`
 
 GROUP BY 
   `World Type`,
   `Steps`,
   `Initial E`,
   `Learning Rate` 
 
 ORDER BY 
   `World Type`,
   `Steps`,
   `Initial E`,
   `Learning Rate`