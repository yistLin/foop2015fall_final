package liardice;

import java.io.Serializable;

public class GameStatus implements Serializable {
	static final int DO_CATCH = 0;
	static final int DO_BID = 1;
	static final int DO_CONTINUE = 2;
	int status;
	GameStatus(int status) {
		this.status = status;
	};
}