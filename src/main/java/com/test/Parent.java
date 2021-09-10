package com.test;

import java.util.*;
import javax.persistence.*;

@Entity
public class Parent
{
	@Id
	@GeneratedValue
	private int id;

	//@OneToMany(mappedBy="parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	//private List<Child> children = new LinkedList<>();

	@OneToOne(mappedBy="padrasto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Child filho;

	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
	{
		this.id = id;
	}
	
	/*public List<Child> getChildren()
	{
		return children;
	}
	
	public void setChildren(List<Child> children)
	{
		this.children = children;
	}*/
}
