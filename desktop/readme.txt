
Description:
	This is an implementation of Gridworld; a toy environment for testing path-finding algorithms. The Gridworld environment is a two-dimensional grid
	where the goal is for an agent to navigate from a start tile to a good exit tile while avoiding bad exits tile. There are three types of tiles; 
	first movable tiles from which the player can move to any directly adjacent tile(no diagonal movement), second exit tiles from which the player 
	only has one action to end the game with either a win or lose depending on the tile's exit value, and lastly empty tiles which the player cannot enter.
	
	This version demonstrates Q-Learning. Q(s, a) is the value of moving from one tile to a neighbor tile, where s is the start tile, a is the movement,
	and s' is the new tile that a brings the player to from s. The current Q value of moving from one tile to a neighbor tile is shown as an integer 
	shifted from the center of the s tile in the direction of the s' tile. An exit tile is displayed with the exit value at the center and a small 
	inner square colored to signify the exit being good or bad. Once a player moves onto an exit tile they are transported back to the start tile. 
	
	At each player movement the program will update the Q value of the state-action pair for the move to be the maximum value of any action at the new 
	state. In this way continued exploration will cause the Q values along the optimal path from any state to the goal state to converge to the goal 
	value.  	 

	The program allows the user to experiment with the simple search strategy, epsilon-greedy, in which with some probably, e, a random move will be 
	chosen, and otherwise the maximum Q value is chosen. By keeping e high at first the agent will be encouraged to explore. As the agent learns more
	about the environment the e value can be lowered so that mostly the agent will follow the optimal path found so far but still leave some room for 
	exploration. A regular greedy search will then always follow the optimal found. 

Interactions:
	Grid:
		Left-Click or Tap	: 		If within tile, cycle the tile's type: movable, good exit, bad exit, empty. 
									  *This can be used during execution to change the structure of the board and even erase parts of the history.
		
		
	Player:
		Up or W 			:		Move player up
		Down or S 			:		Move player down
		Left or A 			:		Move player Left
		Right or D 			:		Move player Right
	
	Search:
		SPACE 				:		Add 1 to searchSteps
		TAB 				:		Add 10 to searchSteps
		PAGE_DOWN 			:		Add 100 to searchSteps
		END		 			:		Add 1000 to searchSteps
		DEL 				:		Stop search
		G					:		Set E to 0
		E					:		Add .1 to E
		
		