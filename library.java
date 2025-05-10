import java.sql.*;
import java.util.Scanner;

public class LibrarySystem {
    private Connection connection;

    public LibrarySystem() {
        connection = DatabaseConnector.connect();
    }

    // Add new book to the library
    public void addBook(String title, String author, String genre, int quantity) {
        String query = "INSERT INTO Books (title, author, genre, quantity) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setString(3, genre);
            stmt.setInt(4, quantity);
            stmt.executeUpdate();
            System.out.println("Book added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Search for a book by title, author, or genre
    public void searchBooks(String searchQuery) {
        String query = "SELECT * FROM Books WHERE title LIKE ? OR author LIKE ? OR genre LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            String search = "%" + searchQuery + "%";
            stmt.setString(1, search);
            stmt.setString(2, search);
            stmt.setString(3, search);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("Book ID: " + rs.getInt("book_id") +
                        ", Title: " + rs.getString("title") +
                        ", Author: " + rs.getString("author") +
                        ", Genre: " + rs.getString("genre") +
                        ", Quantity: " + rs.getInt("quantity"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // View all available books
    public void viewAvailableBooks() {
        String query = "SELECT * FROM Books WHERE quantity > 0";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                System.out.println("Book ID: " + rs.getInt("book_id") +
                        ", Title: " + rs.getString("title") +
                        ", Author: " + rs.getString("author") +
                        ", Genre: " + rs.getString("genre") +
                        ", Quantity: " + rs.getInt("quantity"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Borrow a book
    public void borrowBook(int userId, int bookId) {
        String query = "SELECT quantity FROM Books WHERE book_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt("quantity") > 0) {
                String borrowQuery = "INSERT INTO Transactions (user_id, book_id, borrow_date) VALUES (?, ?, CURDATE())";
                try (PreparedStatement borrowStmt = connection.prepareStatement(borrowQuery)) {
                    borrowStmt.setInt(1, userId);
                    borrowStmt.setInt(2, bookId);
                    borrowStmt.executeUpdate();
                    // Update book quantity after borrowing
                    String updateQuantityQuery = "UPDATE Books SET quantity = quantity - 1 WHERE book_id = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateQuantityQuery)) {
                        updateStmt.setInt(1, bookId);
                        updateStmt.executeUpdate();
                    }
                    System.out.println("Book borrowed successfully!");
                }
            } else {
                System.out.println("Book not available.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Return a borrowed book and calculate late fee
    public void returnBook(int transactionId) {
        String query = "SELECT book_id, borrow_date FROM Transactions WHERE transaction_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, transactionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int bookId = rs.getInt("book_id");
                Date borrowDate = rs.getDate("borrow_date");
                long daysLate = (System.currentTimeMillis() - borrowDate.getTime()) / (1000 * 60 * 60 * 24);
                double lateFee = daysLate > 7 ? (daysLate - 7) * 0.5 : 0.0; // 0.5 per day after 7 days

                String returnQuery = "UPDATE Transactions SET return_date = CURDATE(), late_fee = ? WHERE transaction_id = ?";
                try (PreparedStatement returnStmt = connection.prepareStatement(returnQuery)) {
                    returnStmt.setDouble(1, lateFee);
                    returnStmt.setInt(2, transactionId);
                    returnStmt.executeUpdate();
                    // Update book quantity after returning
                    String updateQuantityQuery = "UPDATE Books SET quantity = quantity + 1 WHERE book_id = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateQuantityQuery)) {
                        updateStmt.setInt(1, bookId);
                        updateStmt.executeUpdate();
                    }
                    System.out.println("Book returned successfully with a late fee of " + lateFee);
                }
            } else {
                System.out.println("Transaction not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // View borrowed books by a user
    public void viewBorrowedBooks(int userId) {
        String query = "SELECT t.transaction_id, b.title, t.borrow_date, t.return_date, t.late_fee FROM Transactions t JOIN Books b ON t.book_id = b.book_id WHERE t.user_id = ? AND t.return_date IS NULL";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("Transaction ID: " + rs.getInt("transaction_id") +
                        ", Book Title: " + rs.getString("title") +
                        ", Borrow Date: " + rs.getDate("borrow_date") +
                        ", Late Fee: " + rs.getDouble("late_fee"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // User registration
    public void registerUser(String name, String email, String phone) {
        String query = "INSERT INTO Users (name, email, phone) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.executeUpdate();
            System.out.println("User registered successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Main method for interacting with the system
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        LibrarySystem librarySystem = new LibrarySystem();

        System.out.println("Library Management System");
        System.out.println("1. Add Book");
        System.out.println("2. Search Books");
        System.out.println("3. View Available Books");
        System.out.println("4. Borrow Book");
        System.out.println("5. Return Book");
        System.out.println("6. View Borrowed Books");
        System.out.println("7. Register User");
        System.out.println("Enter choice:");

        int choice = sc.nextInt();

        switch (choice) {
            case 1:
                sc.nextLine(); // Consume newline
                System.out.println("Enter book title:");
                String title = sc.nextLine();
                System.out.println("Enter book author:");
                String author = sc.nextLine();
                System.out.println("Enter book genre:");
                String genre = sc.nextLine();
                System.out.println("Enter book quantity:");
                int quantity = sc.nextInt();
                librarySystem.addBook(title, author, genre, quantity);
                break;
            case 2:
                sc.nextLine(); // Consume newline
                System.out.println("Enter search query (title/author/genre):");
                String searchQuery = sc.nextLine();
                librarySystem.searchBooks(searchQuery);
                break;
            case 3:
                librarySystem.viewAvailableBooks();
                break;
            case 4:
                System.out.println("Enter user ID:");
                int userId = sc.nextInt();
                System.out.println("Enter book ID:");
                int bookId = sc.nextInt();
                librarySystem.borrowBook(userId, bookId);
                break;
            case 5:
                System.out.println("Enter transaction ID:");
                int transactionId = sc.nextInt();
                librarySystem.returnBook(transactionId);
                break;
            case 6:
                System.out.println("Enter user ID:");
                int borrowUserId = sc.nextInt();
                librarySystem.viewBorrowedBooks(borrowUserId);
                break;
            case 7:
                sc.nextLine(); // Consume newline
                System.out.println("Enter name:");
                String name = sc.nextLine();
                System.out.println("Enter email:");
                String email = sc.nextLine();
                System.out.println("Enter phone number:");
                String phone = sc.nextLine();
                librarySystem.registerUser(name, email, phone);
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }
}
