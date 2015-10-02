package im.actor;

import akka.actor.ActorSystem;
import akka.actor.Props;
import im.actor.botkit.RemoteBot;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter bot token: ");
        System.out.flush();
        String token = scanner.nextLine();
        scanner.close();

        ActorSystem system = ActorSystem.create();

        system.actorOf(Props.create(HelloBot.class, token, RemoteBot.DefaultEndpoint()), "HelloBot");

        system.awaitTermination();
    }
}
