-- Users Table (Already Exists)
CREATE TABLE Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(15)
);

-- Books Table (Modified for Multiple Copies)
CREATE TABLE Books (
    book_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    author VARCHAR(255),
    genre VARCHAR(100),
    total_quantity INT,
    available_quantity INT
);

-- Copies Table (Manages multiple copies of a book)
CREATE TABLE Book_Copies (
    copy_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT,
    status ENUM('AVAILABLE', 'BORROWED', 'RESERVED') DEFAULT 'AVAILABLE',
    FOREIGN KEY (book_id) REFERENCES Books(book_id)
);

-- Transactions Table (Records borrow/return events)
CREATE TABLE Transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    copy_id INT,
    borrow_date DATE,
    return_date DATE,
    late_fee DOUBLE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (copy_id) REFERENCES Book_Copies(copy_id)
);

-- Reservations Table (Handles reserved books)
CREATE TABLE Reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    book_id INT,
    reservation_date DATE,
    status ENUM('PENDING', 'COMPLETED') DEFAULT 'PENDING',
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (book_id) REFERENCES Books(book_id)
);

-- Staff Table (Manages library staff members)
CREATE TABLE Staff (
    staff_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(100),
    branch_id INT
);

-- Branches Table (Manages different library branches)
CREATE TABLE Branches (
    branch_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    location VARCHAR(255)
);

-- Notifications Table (Tracks notifications for users)
CREATE TABLE Notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    message TEXT,
    status ENUM('SENT', 'PENDING') DEFAULT 'PENDING',
    date_sent DATE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);
