# Agent Speak Pommerman

## Abstract

The aim of this project is to implement Pommerman, a modified version of
Nintendo’s classic game Bomberman (1983). Pommerman features four
opposing agents that compete in a free-for-all (FFA) battle. Each agent’s
objective is to strategically place bombs on the board to eliminate oppo-
nents, collect power-ups, and evade the explosions from other agents’ bombs,
ultimately striving to be the last agent standing. The project utilizes AgentS-
peak, an agent-oriented programming language, along with its interpreter,
Jason, which together provide a suitable framework for developing multi-
agent systems.

## Running the project

Before cloning the Git repository of the project, the following dependencies need to be
installed:
* Java version 19.0.2
* Jason CLI version 3.2.0
* Gradle version 8.0.1

Once the dependencies have been correctly installed, one can proceed to clone the Git
repository following the instructions below. Open a terminal or command prompt on
the local machine. Navigate to the desired directory for repository cloning, utilizing the
cd command to switch directories:

```
cd path/to/your/directory
```

The following command should be executed to clone the repository:

```
git clone https://gitlab.com/pika-lab/courses/mas/projects/
mas-project-morselli-vorabbi-ay2223.git
```

The command will download the repository to the local machine. If the repository is
private, users might be prompted to enter their GitLab username and password or use
an access token.

```
Username for ’https://gitlab.com’: your_username
Password for ’https://your_username@gitlab.com’:
```

Credentials should be entered to proceed. Once the cloning process is complete, pro-
ceed to enter the cloned directory.
```
cd mas-project-morselli-vorabbi-ay2223
```

Now, execute the following command to run the Multi Agent System:

```
jason pommerman.mas2j
```
