package ru.romanzaycev.jogltest;

public class Main {
    public static void main(String[] args) {
        System.out.println("Start");
        Game game = new Game();

        try {
            game.play();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
