package com.galapea.demogriddb;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.toshiba.mwcloud.gs.Collection;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.RowKey;
import com.toshiba.mwcloud.gs.RowSet;

@Component
public class AppInitializer implements CommandLineRunner {

    @Autowired
    private GridStore store;

    static class Person {
        @RowKey
        String name;
        boolean status;
        long count;
        byte[] lob;
    }

    @Override
    public void run(String... args) throws Exception {
        // testCollection();
    }

    private void testCollection() throws GSException {
        System.out.println("run commandlinerunner");
        // Creating a collection
        Collection<String, Person> personCollection = store.putCollection("col01", Person.class);

        // Setting an index for a column
        personCollection.createIndex("count");

        // Setting auto-commit off
        personCollection.setAutoCommit(false);

        // Preparing row data
        Person person = new Person();
        person.name = "name01";
        person.status = false;
        person.count = 1;
        person.lob = new byte[] {65, 66, 67, 68, 69, 70, 71, 72, 73, 74};

        // Operating a row in KV format: RowKey is "name01"
        boolean update = true;
        personCollection.put(person); // Registration
        person = personCollection.get(person.name, update); // Aquisition (Locking to update)
        personCollection.remove(person.name); // Deletion

        // Operating a row in KV format: RowKey is "name02"
        personCollection.put("name02", person); // Registration (Specifying RowKey)

        // Committing transaction (releasing lock)
        personCollection.commit();

        // Search rows in a container
        Query<Person> query = personCollection.query("select * where name = 'name02'");

        // Fetching and updating retrieved rows
        RowSet<Person> rs = query.fetch(update);
        while (rs.hasNext()) {
            // Update the searched Row
            Person person1 = rs.next();
            person1.count += 1;
            rs.update(person1);

            System.out.println("Person:" + " name=" + person1.name + " status=" + person1.status
                    + " count=" + person1.count + " lob=" + Arrays.toString(person1.lob));
        }

        // Committing transaction
        personCollection.commit();
    }

}
