CREATE VIEW `Average Per Policy Per World` AS 

SELECT 
  `Search Policy` AS `Search Policy`,
  `World Type` AS `World Type`,
  `Steps` AS `Steps`,`Initial E` AS `Initial E`,
  `Learning Rate` AS `Learning Rate`,
  AVG(`Best Found Path`) AS `AVG(``Best Found Path``)` 

FROM `GridworldResults` 

GROUP BY 
  `Search Policy`,
  `World Type`,
  `Steps`,
  `Initial E`,
  `Learning Rate` 

ORDER BY 
  `Search Policy`,
  `World Type`,
  `Initial E`,
  `Learning Rate`,
  `Steps`