import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        String nigga;
        try(Scanner sc = new Scanner(System.in)) {
            boolean exit = false;
            System.out.println("Insert a string: ");
            while(!exit){
                if(sc.hasNext()){
                    nigga = sc.next();
                    if(nigga.equals("exit")){
                        exit = true;
                    }
                    System.out.println(nigga);
                }
                else {
                    System.out.println("Invalid input");
                }
            }
        }
        catch(Exception e){
            System.out.println("Error:" + e.getMessage());
        }





    }
}