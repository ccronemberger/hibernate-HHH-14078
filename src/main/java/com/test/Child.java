package com.test;

import javax.persistence.*;

@Entity
public class Child
{
	@Id
	@GeneratedValue
	private int id;

	@ManyToOne
	private Parent parent;

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public Parent getParent()
	{
		return parent;
	}

	public void setParent(Parent parent)
	{
		this.parent = parent;
	}
}
