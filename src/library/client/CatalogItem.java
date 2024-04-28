package library.client;

import org.bson.types.ObjectId;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CatalogItem {
	private final StringProperty title;
	private final StringProperty author;
	private final StringProperty type;
	private final BooleanProperty available;
	private final StringProperty itemId;
	private ObjectId borrowerId;
	private String borrower;
	private String dueDate;
	private String dateBorrowed;
	private String filePath;
	private final IntegerProperty rating ;
    private final IntegerProperty holdCount ;
    private int dummyRating = 0;
    
    
	public CatalogItem(String title, String author, String type, boolean available, String itemId ,int rating,int holdCount ) {
		this.title = new SimpleStringProperty(title);
		this.author = new SimpleStringProperty(author);
		this.type = new SimpleStringProperty(type);
		this.available = new SimpleBooleanProperty(available);
		this.itemId = new SimpleStringProperty(itemId);
		this.rating = new SimpleIntegerProperty(rating);
		this.holdCount = new SimpleIntegerProperty(holdCount);
	}
	
	public final IntegerProperty ratingNumberProperty() {
		return rating;
	}
	
	public int getDummyRating() {
		return dummyRating;
	}
	
	public void setDummyRating(int dummyRating) {
		this.dummyRating = dummyRating;
	}

    public final IntegerProperty holdCountProperty() {
        return holdCount;
    }

    
    
    public int getHoldCount() {
        return holdCount.get();
    }

    public void setHoldCount(int holdCount) {
        this.holdCount.set(holdCount);
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

	public StringProperty itemIdProperty() {
		return itemId;
	}

	public BooleanProperty availableProperty() {
		return available;
	}

	// Getters and setters for the fields
	public String getTitle() {
		return title.get();
	}

	public void setTitle(String title) {
		this.title.set(title);
	}

	public String getItemId() {
		return itemId.get();
	}

	public void setItemId(String itemId) {
		this.itemId.set(itemId);
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

	public boolean isAvailable() {
		return available.get();
	}

	public void setAvailable(boolean available) {
		this.available.set(available);
	}

	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}

	public String getDueDate() {
		return dueDate;
	}

	/**
	 * Retrieves the book content associated with this CatalogItem.
	 * 
	 * @return the book content as a byte array.
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * Sets the book content for this CatalogItem.
	 * 
	 * @param bookContent the book content as a byte array.
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

//	public Object getImageUrl() {
//		// TODO Auto-generated method stub
//		return imageUrl;
//	}

	public ObjectId getBorrowerId() {
		return borrowerId;
	}

	public void setBorrowerId(ObjectId borrowerId) {
		this.borrowerId = borrowerId;
	}

	public void setDateBorrowed(String dateBorrowed) {
		this.dateBorrowed = dateBorrowed;
	}

	public String getDateBorrowed() {
		return dateBorrowed;
	}

	public void setBorrower(String borrower) {
		this.borrower = borrower;
	}

	public String getBorrower() {
		return borrower;
	}
}
