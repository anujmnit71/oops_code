package com.java.ingredient.javaIngredient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Ingredient class javadoc
 *
 */
public class Ingredient implements Comparable<Ingredient>
{
	String name;
	int grams;
    public Ingredient(String name , int grams){
    	this.name = name;
    	this.grams = grams;
    }
    
    @Override
	public String toString(){
    	return "[" + name + " (" + grams + ") ]";
    }

    @Override
    public int hashCode(){
    	return grams + name.length();
    }
    
    @Override
    public boolean equals(Object other){
    	if(this == other)
    		return true;
    	
    	if(!(other instanceof Ingredient))
    		return false;
    	
    	Ingredient ing = (Ingredient)other;
    	if(name.equals(ing.name) && grams == ing.grams )
    		return true;
    	return false;
    }
    
	public int compareTo(Ingredient other) {
		if(name.compareTo(other.name) == 0){
			return grams - other.grams;
		}
		return name.compareTo(other.name);
	}
    
	public static void collectionDemo() {
		Ingredient a = new Ingredient("Apple", 112);
		Ingredient b = new Ingredient("Bannana", 236);
		Ingredient b2 = new Ingredient("Bannana", 100);
		List<Ingredient> ingredients = new ArrayList<Ingredient>();
		ingredients.add(b2);
		ingredients.add(a);
		ingredients.add(b);
		System.out.println("ingredients:" + ingredients);
		// ingredients:[Bannana (100 grams), Apple (112 grams), Bannana (236 grams)]
		// note: uses the collection [ ... ] built-in toString()
		Collections.sort(ingredients);
		System.out.println("sorted:" + ingredients);
		// sorted:[Apple (112 grams), Bannana (100 grams), Bannana (236 grams)]
		//Collections.sort(ingredients, new SortByGrams());
		System.out.println("sorted by grams:" + ingredients);
		// sorted by grams:[Bannana (100 grams), Apple (112 grams), Bannana (236 grams)]
		System.out.println("max:" + Collections.max(ingredients));
	}
	
	public static void main(String args[]) {
		Ingredient a = new Ingredient("Apple", 112);
		Ingredient b = new Ingredient("Bannana", 236);
		Ingredient b2 = new Ingredient("Bannana", 236); // deeply the same as b
		System.out.println(a); // calls toString()
		System.out.println(b);
		System.out.println("Apple eq Bannana:" + (a.equals(b))); // false
		System.out.println("Bannana eq Bannana2:" + (b.equals(b2))); // true
		System.out.println("Apple hash:" + a.hashCode()); // 167
		System.out.println("Bannana hash:" + b.hashCode()); // 313
		System.out.println("Bannana2 hash:" + b2.hashCode()); // 313 (!)
		collectionDemo();
	}

	

}
