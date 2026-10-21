import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        Scanner myObj = new Scanner(System.in);

        String id, pw, email, fname, lname;

        System.out.println("Enter first name");
        fname = myObj.nextLine();

        System.out.println("Enter last name");
        lname = myObj.nextLine();

        System.out.println("Enter email");
        email = myObj.nextLine();

        System.out.println("Enter id");
        id = myObj.nextLine();

        System.out.println("Enter password");
		pw = myObj.nextLine(); //There should be some regulations about id and pw

		System.out.println("Enter password again");
		String temp = myObj.nextLine();

		while (!temp.equals(pw)) {
    		System.out.println("Password incorrect. Try again:");
    		temp = myObj.nextLine();
		}

		System.out.println("Password correct!");
        
        myObj.close();
    }
}