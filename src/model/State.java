package model;

public enum State {
	/*MOVING_N,
	MOVING_S,
	MOVING_E,
	MOVING_W,
	MOVING_NE,
	MOVING_SE,
	MOVING_NW,
	MOVING_SW,*/
	
	BALL_N,
	BALL_S,
	BALL_W,
	BALL_E,
	
	TOUCHLINE_N,
	TOUCHLINE_S,
	TOUCHLINE_W,
	TOUCHLINE_E,

	TEAM_MATE_N,
	TEAM_MATE_S,
	TEAM_MATE_W,
	TEAM_MATE_E,
	
	OPPONENT_N,
	OPPONENT_S,
	OPPONENT_W,
	OPPONENT_E,
	
	OPP_GOALS_CLOSE,
	
	OWN_GOALS_CLOSE,

	IN_POSS,
	TM_IN_POSS
	//IN_COLL,
}
