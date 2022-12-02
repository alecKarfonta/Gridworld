# Gridworld

Here is an implemenation of Gridworld. It is a toy environment for visualizing reinforcement learning algorithms like Q-Learning.

An agent can move up, down, left or right, as long as there is a tile to move to. There are three different types of tiles. First
are regular active tiles that the agent can use. Then there are exit tiles which end the game, these can have positive or negative rewards. 
Finally there are disabled tiles where the player cannot move. 

The goal of the agent is to learn to navigate the Gridworld in order to achieve positive rewards. 

In this interface you can do many things like customize the Gridworld and agent's learning parameters. Further you can use this to run many
experiments on competing search policies and store the results for futher analysis. 

The program is writen in Java using the Libgdx game development framework for the visuals. It is a Gradle project. I would recommend 
using Eclipse to build and import as a Gradle project. There should a number of individual projects all starting with Gridworld. You will
need at least Gridworld, Gridworld-core and Gridworld-desktop. The bulk of the code resides in Gridworld-core, and then the launcher for 
running the interface on a desktop is in Gridworld-desktop. See tutorials on Libgdx for more information.

Interface:
https://www.youtube.com/watch?v=fvkCYeAMq6M

Cliff example:
https://www.youtube.com/watch?v=ppALjH0kYPE
