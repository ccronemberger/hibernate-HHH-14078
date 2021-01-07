package com.test;

import java.util.*;
import javax.persistence.*;

@Entity
public class Parent
{
	@Id
	@GeneratedValue
	private int id;

	@OneToMany(mappedBy="parent")
	private List<Child> children = new LinkedList<>();

	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
	{
		this.id = id;
	}
	
	public List<Child> getChildren()
	{
		return children;
	}
	
	public void setChildren(List<Child> children)
	{
		this.children = children;
	}
}
