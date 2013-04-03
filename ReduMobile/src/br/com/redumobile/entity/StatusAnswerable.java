package br.com.redumobile.entity;

import java.util.ArrayList;
import java.util.Date;


public abstract class StatusAnswerable extends Status {
	private ArrayList<Answer> answers;
	private ReduEntityWithWall statusable;

	public StatusAnswerable(Date createdAt, Date updatedAt, int id,
			String text, User user, ArrayList<Answer> answers,
			ReduEntityWithWall statusable) {
		super(createdAt, updatedAt, id, text, user);

		this.answers = answers;

		this.statusable = statusable;
	}

	public ArrayList<Answer> getAnswers() {
		return answers;
	}

	public ReduEntityWithWall getStatusable() {
		return statusable;
	}

	public void setAnswers(ArrayList<Answer> answers) {
		this.answers = answers;
	}

	public void setStatusable(ReduEntityWithWall statusable) {
		this.statusable = statusable;
	}
}
