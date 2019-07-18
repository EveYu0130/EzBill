package ca.uwaterloo.cs446.ezbill;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class GroupAccountBookListFragment extends AccountBookListFragmentTemplate {

    public GroupAccountBookListFragment() {}

    @Override
    public void addDateToView(Model model, ArrayList<String> dates, RecyclerView Rv) {
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration();
        for (GroupAccountBook groupAccountBook : model.getGroupAccountBookList()) {
            dates.add(groupAccountBook.getEndDate());
        }
        dividerItemDecoration.setDates(dates);
        Rv.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public void addAccountBookInfoToView(Model model, RecyclerView Rv) {
        ArrayList<AccountBook> accountBooks = new ArrayList<>();
        for (GroupAccountBook groupAccountBook : model.getGroupAccountBookList()) {
            AccountBook accountBook = groupAccountBook;
            accountBooks.add(accountBook);
        }
        TimeAdapter myAdapter = new TimeAdapter(getActivity(), accountBooks, "Group");
        Rv.setAdapter(myAdapter);
    }
}