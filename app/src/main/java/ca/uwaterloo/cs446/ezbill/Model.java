package ca.uwaterloo.cs446.ezbill;


import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.annotation.Nonnull;

public class Model extends Observable {

    private static Model modelInstance = new Model();
    static Model getInstance() {
        return modelInstance;
    }
//    private ArrayList<Participant> participantList;

    boolean mainPageGroupViewOnSelect;

    ArrayList<GroupAccountBook> groupAccountBookList;
    ArrayList<IndividualAccountBook> individualAccountBookList;
    String currentUserId;
    String currentUsername;
    String clickedAccountBookId;
    ArrayList<GroupTransaction> currentGroupTransactionList;
    String userEmail = "alice@gmail.com";
    boolean viewAllBillClicked;

    Model() {
        groupAccountBookList = new ArrayList<>();
        individualAccountBookList = new ArrayList<>();
        currentGroupTransactionList = new ArrayList<>();
        mainPageGroupViewOnSelect = true;
        viewAllBillClicked = false;
        readFromDB();
    }


    public boolean getViewAllBillClicked() {
        return viewAllBillClicked;
    }

    public void setViewAllBillClicked(boolean isClicked) {
        viewAllBillClicked = isClicked;
        setChanged();
        notifyObservers();

    }

    public ArrayList<GroupTransaction> getCurrentGroupTransactionList() {
        return currentGroupTransactionList;
    }

    public void addToCurrentGroupTransactionList(GroupTransaction newTransaction) {
        currentGroupTransactionList.add(newTransaction);
        GroupAccountBook groupAccountBook = getGroupAccountBook(clickedAccountBookId);
        groupAccountBook.setMyExpense(calculateMyExpense(clickedAccountBookId));
        groupAccountBook.setMyExpense(calculateTotalExpense(clickedAccountBookId));
        addTransactionToDB(newTransaction);
        Collections.sort(currentGroupTransactionList);

        setChanged();
        notifyObservers();
    }

    public void addToCurrentGroupAccountBookList(GroupAccountBook newGroupAccountBook, String email, String userId, String username) {
        groupAccountBookList.add(newGroupAccountBook);
        addAccountBookToDB(newGroupAccountBook, "Group", email, userId, username);
        Collections.sort(groupAccountBookList);

        setChanged();
        notifyObservers();
    }

    public void addToCurrentIndividualAccountBookList(IndividualAccountBook newGroupAccountBook, String email, String userId, String username) {
        individualAccountBookList.add(newGroupAccountBook);
        addAccountBookToDB(newGroupAccountBook, "Individual", email, userId, username);
        Collections.sort(individualAccountBookList);

        setChanged();
        notifyObservers();
    }


    public ArrayList<GroupAccountBook> getGroupAccountBookList() {
        return groupAccountBookList;
    }

    public ArrayList<IndividualAccountBook> getIndividualAccountBookList() {
        return  individualAccountBookList;
    }

    public GroupAccountBook getGroupAccountBook(String id) {
        for (GroupAccountBook groupAccountBook : groupAccountBookList) {
            if (groupAccountBook.getId().equals(id)) {
                return groupAccountBook;
            }
        }
        return null;
    }

    public void addGroupAccountBook(GroupAccountBook groupAccountBook) {
        groupAccountBookList.add(groupAccountBook);
        Collections.sort(groupAccountBookList);
    }

    public void addIndividualAccountBook(IndividualAccountBook individualAccountBook) {
        individualAccountBookList.add(individualAccountBook);
        Collections.sort(individualAccountBookList);
    }

    public boolean hasGroupAccountBook(String id) {
        for (GroupAccountBook groupAccountBook : groupAccountBookList) {
            if (groupAccountBook.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasIndividualAccountBook(String id) {
        for (IndividualAccountBook individualAccountBook : individualAccountBookList) {
            if (individualAccountBook.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasGroupTransaction(String id) {
        for (GroupTransaction groupTransaction : currentGroupTransactionList) {
            if (groupTransaction.getUuid().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public void addGroupTransaction(GroupTransaction groupTransaction) {
        currentGroupTransactionList.add(groupTransaction);
        Collections.sort(currentGroupTransactionList);
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public void setCurrentUsername(String currentUsername) {
        this.currentUsername = currentUsername;
    }

    public String getClickedAccountBookId() {
        return clickedAccountBookId;
    }

    public void setClickedAccountBookId(String clickedAccountBookId) {
        this.clickedAccountBookId = clickedAccountBookId;
    }

    public void initObservers() {
        setChanged();
        notifyObservers();
    }

    public void addParticipant(String id) {
        GroupAccountBook groupAccountBook = getGroupAccountBook(id);
        groupAccountBook.addParticipant(new Participant());
        setChanged();
        notifyObservers();

    }

    public ArrayList<Participant> getParticipantsById(String id) {
        GroupAccountBook groupAccountBook = getGroupAccountBook(id);
        return groupAccountBook.getParticipantList();
    }

    public float calculateMyExpense(String id) {
        float totalAmount = 0;
        for (GroupTransaction groupTransaction : currentGroupTransactionList) {
            for (HashMap<Participant, Float> participant :  groupTransaction.getParticipants()) {
                for (HashMap.Entry<Participant,Float> entry : participant.entrySet()) {
                    Participant key = entry.getKey();
                    Float value = entry.getValue();
                    if (key.getId().equals(currentUserId)) {
                        totalAmount += value;
                    }
                }
            }
        }
        return totalAmount;
    }

    public float calculateTotalExpense(String id) {
        float totalAmount = 0;
        for (GroupTransaction groupTransaction : currentGroupTransactionList) {
            totalAmount += groupTransaction.getAmount();
        }
        return totalAmount;
    }

    public String getUsername(String id) {
        if (id.equals("U1")) {
            return "Alice";
        } else if (id.equals("U2")) {
            return "Bob";
        } else if (id.equals("U3")) {
            return "Carol";
        } else {
            return "David";
        }
    }

    public Date parseStringToDate(String date) throws Exception{
        DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        Date parsedDate = (Date) formatter.parse(date);
        return parsedDate;
    }

    public String parseDateToString(Date date) {
        DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        String formattedDate = formatter.format(date);
        return formattedDate;
    }

    public boolean isMainPageGroupViewOnSelect() {
        return mainPageGroupViewOnSelect;
    }

    public void setMainPageGroupViewOnSelect(boolean mainPageGroupViewOnSelect) {
        this.mainPageGroupViewOnSelect = mainPageGroupViewOnSelect;
    }

    public void readFromDB() {
        // Access a Cloud Firestore instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();

//      read data from database
        db.collection("user_account_book_info")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                setCurrentUserId(document.getData().get("userId").toString());
                                setCurrentUsername(document.getData().get("username").toString());

                                String accountBookId = document.getData().get("accountBookId").toString();
                                String accountBookName = document.getData().get("accountBookName").toString();
                                String startDate = document.getData().get("accountBookStartDate").toString();
                                String endDate = document.getData().get("accountBookEndDate").toString();
                                String defaultCurrency = document.getData().get("accountBookCurrency").toString();
                                String type = document.getData().get("accountBookType").toString();

                                if (type.equals("Group")) {
                                    if (!hasGroupAccountBook(accountBookId)) {
                                        GroupAccountBook groupAccountBook = new GroupAccountBook(accountBookId, accountBookName, startDate, endDate, defaultCurrency);
                                        addGroupAccountBook(groupAccountBook);
                                    }
                                } else {
                                    if (!hasIndividualAccountBook(accountBookId)) {
                                        IndividualAccountBook individualAccountBook = new IndividualAccountBook(accountBookId, accountBookName, startDate, endDate, defaultCurrency);
                                        addIndividualAccountBook(individualAccountBook);
                                    }
                                }
                                Log.d("READ", document.getId() + " => " + document.getData());
                            }

                            setChanged();
                            notifyObservers();

                        } else {
                            Log.w("READ", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public void readTransactionsFromDB() {
        // Access a Cloud Firestore instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // read data from database
        db.collection("transactions")
                .whereEqualTo("accountBookId", getClickedAccountBookId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@Nonnull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String transactionId = document.getData().get("id").toString();

                                if (!hasGroupTransaction(transactionId)) {
                                    String category = document.getData().get("category").toString();
                                    float amount = Float.valueOf(document.getData().get("amount").toString());
                                    String creatorId = document.getData().get("creator").toString();
                                    String creatorName = getUsername(creatorId);
                                    Participant creator = new Participant(creatorId, creatorName);
                                    String date = document.getData().get("date").toString();
                                    String note = document.getData().get("note").toString();
                                    String payerId = document.getData().get("payer").toString();
                                    String payerName = getUsername(payerId);
                                    Participant payer = new Participant(payerId, payerName);
                                    String type = document.getData().get("type").toString();
                                    String currency = document.getData().get("currency").toString();
                                    String data = document.getData().get("participant").toString();
                                    data = data.substring(1,data.length()-2);
                                    ArrayList<HashMap<Participant, Float>> participants = new ArrayList<>();
                                    String[] pairs = data.split(",");
                                    for (int i=0;i<pairs.length;i++) {

                                        HashMap<Participant, Float> map = new HashMap<>();
                                        String pair = pairs[i];
                                        String[] keyValue = pair.split("=");
                                        Participant participant = new Participant(keyValue[0], getUsername(keyValue[0]));
                                        map.put(participant, Float.valueOf(keyValue[1]));
                                        participants.add(map);
                                    }

                                    GroupTransaction groupTransaction = new GroupTransaction(transactionId, category, type, amount, currency, note, date, creator, payer, participants);
                                    addGroupTransaction(groupTransaction);
                                }

                                Log.d("READ", document.getId() + " => " + document.getData());
                            }
                            getGroupAccountBook(getClickedAccountBookId()).setMyExpense(calculateMyExpense(getClickedAccountBookId()));
                            getGroupAccountBook(getClickedAccountBookId()).setGroupExpense(calculateTotalExpense(getClickedAccountBookId()));

                            setChanged();
                            notifyObservers();
                        } else {
                            Log.w("READ", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public void addTransactionToDB(GroupTransaction groupTransaction) {
        // Access a Cloud Firestore instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
        // Create a new user with a first and last name
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("accountBookId", clickedAccountBookId);
        transaction.put("id", groupTransaction.getUuid());
        transaction.put("category", groupTransaction.getCategory());
        transaction.put("type", groupTransaction.getType());
        transaction.put("amount", groupTransaction.getAmount().toString());
        transaction.put("currency", groupTransaction.getCurrency());
        transaction.put("note", groupTransaction.getNote());
        transaction.put("date", groupTransaction.getDate());
        transaction.put("creator", groupTransaction.getCreator().getId());
        transaction.put("payer", groupTransaction.getPayer().getId());
        Map<String, Object> participant = new HashMap<>();
        for (HashMap<Participant, Float> data : groupTransaction.getParticipants()) {
            for (HashMap.Entry<Participant,Float> entry : data.entrySet()) {
                participant.put(entry.getKey().getId(), entry.getValue());
            }
        }
        transaction.put("participant", participant);

        // Add a new document with a generated ID
        db.collection("transactions")
                .add(transaction)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("WRITE", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@Nonnull Exception e) {
                        Log.w("WRITE", "Error adding document", e);
                    }
                });
    }

    public void addAccountBookToDB(AccountBook accountBook, String type, String email, String userId, String username) {
        // Access a Cloud Firestore instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
        Map<String, Object> ab = new HashMap<>();
        ab.put("accountBookId", accountBook.getId());
        ab.put("accountBookName", accountBook.getName());
        ab.put("accountBookCurrency", accountBook.getDefaultCurrency());
        ab.put("accountBookStartDate", accountBook.getStartDate());
        ab.put("accountBookEndDate", accountBook.getEndDate());
        ab.put("accountBookType", type);
        ab.put("email", email);
        ab.put("userId", userId);
        ab.put("username", username);

        // Add a new document with a generated ID
        db.collection("user_account_book_info")
                .add(ab)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("WRITE", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@Nonnull Exception e) {
                        Log.w("WRITE", "Error adding document", e);
                    }
                });
    }


    @Override
    public synchronized void deleteObserver(Observer o) {
        super.deleteObserver(o);
    }

    @Override
    public synchronized void addObserver(Observer o) {
        super.addObserver(o);
    }


    @Override
    public synchronized void deleteObservers() {
        super.deleteObservers();
    }

    @Override
    public void notifyObservers() {
        super.notifyObservers();
    }

}