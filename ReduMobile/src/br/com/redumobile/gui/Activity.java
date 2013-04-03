package br.com.redumobile.gui;

import java.util.ArrayList;
import java.util.Date;

import br.com.redumobile.entity.Answer;
import br.com.redumobile.entity.ReduEntityWithWall;
import br.com.redumobile.entity.StatusAnswerable;
import br.com.redumobile.entity.User;

public final class Activity extends StatusAnswerable {
	public Activity(Date createdAt, Date updatedAt, int id, String text,
			User user, ArrayList<Answer> answers, ReduEntityWithWall statusable) {
		super(createdAt, updatedAt, id, text, user, answers, statusable);
	}
}