package library.client;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.bson.types.ObjectId;

public class BorrowedItem {
	private final StringProperty title;
	private final StringProperty author;
	private final StringProperty type;
	private final StringProperty borrower;
	private final StringProperty dueDate;
	private final StringProperty dateBorrowed;
	private  final StringProperty dateReturned;
	private ObjectId borrowerId;

	// Add a new constructor with the expected signature
	public BorrowedItem(String title, String author, String type, String borrower, String dueDate, String dateBorrowed,
			ObjectId borrowerId, String dateReturned) {
		this.title = new SimpleStringProperty(title);
		this.author = new SimpleStringProperty(author);
		this.type = new SimpleStringProperty(type);
		this.borrower = new SimpleStringProperty(borrower);
		this.dueDate = new SimpleStringProperty(dueDate);
		this.dateBorrowed = new SimpleStringProperty(dateBorrowed);
		this.borrowerId = borrowerId;
		this.dateReturned = new SimpleStringProperty(dateReturned);
	}

	// Getters for observable properties
	public StringProperty titleProperty() {
		return title;
	}

	public StringProperty authorProperty() {
		return author;
	}

	public StringProperty typeProperty() {
		return type;
	}

	public StringProperty borrowerProperty() {
		return borrower;
	}

	public ObjectId getBorrowerId() {
		return borrowerId;
	}

	public void setBorrowerId(ObjectId borrowerId) {
		this.borrowerId = borrowerId;
	}

	public void setDateReturned(String fdateReturned) {
		System.out.println("date returned: " + fdateReturned);
		this.dateReturned.set(fdateReturned);
	}

	public String getBorrower() {
		return borrower.get();
	}

	public void setBorrower(String borrower) {
		this.borrower.set(borrower);
	}

	public StringProperty dueDateProperty() {
		return dueDate;
	}

	public StringProperty dateBorrowedProperty() {
		return dateBorrowed;
	}

	// Getters and setters for the fields
	public String getTitle() {
		return title.get();
	}

	public void setTitle(String title) {
		this.title.set(title);
	}

	public String getAuthor() {
		return author.get();
	}

	public void setAuthor(String author) {
		this.author.set(author);
	}

	public String getType() {
		return type.get();
	}

	public void setType(String type) {
		this.type.set(type);
	}

	public StringProperty dateReturnedProperty() {
		// TODO Auto-generated method stub
		return dateReturned;
	}

}