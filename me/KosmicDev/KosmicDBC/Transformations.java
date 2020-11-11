package me.KosmicDev.KosmicDBC;

public class Transformations {
	String name;
	String displayName;
    PlayerOrientedRunnable functions;
    Float damage;
    Float defense;
    Float dodge;
    Integer id;

    private static int nextId = 0;

    public Transformations(String name, String displayName, PlayerOrientedRunnable functions, Float damage, Float defense, Float dodge) {
        this.name = name;
        this.displayName = displayName;
        this.functions = functions;
        this.damage = damage;
        this.defense = defense;
        this.dodge = dodge;
        this.id = nextId;
        nextId++;
    }
}