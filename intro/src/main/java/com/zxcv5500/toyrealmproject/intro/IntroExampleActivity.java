package com.zxcv5500.toyrealmproject.intro;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zxcv5500.toyrealmproject.R;
import com.zxcv5500.toyrealmproject.intro.model.Cat;
import com.zxcv5500.toyrealmproject.intro.model.Dog;
import com.zxcv5500.toyrealmproject.intro.model.Person;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;

public class IntroExampleActivity extends Activity {
    public static final String TAG = IntroExampleActivity.class.getSimpleName();

    private LinearLayout rootLayout;

    private Realm realm;

    private RealmResults<Person> persons;

    // OrderedRealmCollectionChangeListener receives fine-grained changes - insertions, deletions, and changes.
    // If the change set isn't needed, then RealmChangeListener can also be used.
    private final OrderedRealmCollectionChangeListener<RealmResults<Person>> realmChangeListener = (people, changeSet) -> {
        String insertions = changeSet.getInsertions().length == 0 ? "" : "\n - Insertions: " + Arrays.toString(changeSet.getInsertions());
        String deletions = changeSet.getDeletions().length == 0 ? "" : "\n - Deletions: " + Arrays.toString(changeSet.getDeletions());
        String changes = changeSet.getChanges().length == 0 ? "" : "\n - Changes: " + Arrays.toString(changeSet.getChanges());
        showStatus("Person was loaded, or written to. " + insertions + deletions + changes);
    };

    /* 위 람다 코드를 이전 버전 코드로 환산 시 원형이 다음과 같음
    private final OrderedRealmCollectionChangeListener<RealmResults<Person>> realmChangeListener1 = new OrderedRealmCollectionChangeListener<RealmResults<Person>>() {
        @Override
        public void onChange(RealmResults<Person> people, OrderedCollectionChangeSet changeSet) {

        }
    };
    */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_basic_example);
        rootLayout = findViewById(R.id.container);
        rootLayout.removeAllViews();

        // Clear the Realm if the example was previously run.
        Realm.deleteRealm(Realm.getDefaultConfiguration());

        realm = Realm.getDefaultInstance();

        persons = realm.where(Person.class).findAllAsync();

        persons.addChangeListener(realmChangeListener);

        basicCRUD(realm);
        basicQuery(realm);
        basicLinkQuery(realm);

        // More complex operations can be executed on another thread.
        new ComplexBackgroundOperations(this).execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        persons.removeAllChangeListeners();
        realm.close();
    }

    private void showStatus(String text) {
        Log.i(TAG, text);
        TextView textView = new TextView(this);
        textView.setText(text);
        rootLayout.addView(textView);
    }

    private void basicCRUD(Realm realm) {
        showStatus("Perform basic Create/Read/Update/Delete (CRUD) operations...");


        // All writes must be wrapped in a transaction to facilitate safe multi threading
        realm.executeTransaction(r->{
            Person person = r.createObject(Person.class, 1);
            person.setName("Young Person");
            person.setAge(14);

            person.getPhoneNumbers().add("+1 123 4567");
        });

        /* 위 람다 코드를 이전 버전 코드로 환산 시 원형이 다음과 같음
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

            }
        });
        */


        // Find the first person (no query conditions) and read a field
        final Person person = realm.where(Person.class).findFirst();
        showStatus(person.getName() + ":" + person.getAge());

        // Update person in a transaction
        realm.executeTransaction(r->{
            // Managed objects can be modified inside transactions.
            person.setName("Senior Person");
            person.setAge(99);
            showStatus(person.getName() + "got older: " + person.getAge());
        });
        // Delete all persons
        showStatus("Deleting all psersons");
        realm.executeTransaction(r->r.delete(Person.class));
    }

    private void basicQuery(Realm realm) {
        showStatus("\nPerforming basic Query operation...");

        realm.executeTransaction(r->{
            Person oldPerson = new Person();
            oldPerson.setId(99);
            oldPerson.setAge(99);
            oldPerson.setName("George");
            realm.insertOrUpdate(oldPerson);
        });

        showStatus("Number of persons: " + realm.where(Person.class).count());

        RealmResults<Person> results = realm.where(Person.class).equalTo("age", 99).findAll();

        showStatus("Size of result set : " + results.size());
    }

    private void basicLinkQuery(Realm realm) {
        showStatus("\nPerforming basic Link Query operation...");

        //* 람다식 사용
        realm.executeTransaction(r->{
            Person catLady = realm.createObject(Person.class, 24);
            catLady.setAge(52);
            catLady.setName("Mary");

            Cat tiger = realm.createObject(Cat.class);
            tiger.name = "Tiger";
            catLady.getCats().add(tiger);
        });
        //*/

        /* 위의 람다식을 풀어 쓴 부분이다.
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm r) {
                Person catLady = realm.createObject(Person.class, 24);
                catLady.setAge(52);
                catLady.setName("Mary");

                Cat tiger = realm.createObject(Cat.class);
                tiger.name = "Tiger";
                catLady.getCats().add(tiger);
            }
        });
        //*/

        showStatus("Number of persons: " + realm.where(Person.class).count());

        RealmResults<Person> results = realm.where(Person.class).equalTo("cats.name", "Tiger").findAll();

        showStatus("Size of result set: " + results.size());
    }

    private static class ComplexBackgroundOperations extends AsyncTask<Void, Void, String> {

        private WeakReference<IntroExampleActivity> weakReference;

        public ComplexBackgroundOperations(IntroExampleActivity introExampleActivity) {
            this.weakReference = new WeakReference<>(introExampleActivity);
        }

        @Override
        protected void onPreExecute() {
            IntroExampleActivity activity = weakReference.get();
            if (activity == null) {
                return;
            }
            activity.showStatus("\n\nBeginning complex operations on background thread");
        }

        @Override
        protected String doInBackground(Void... voids) {
            IntroExampleActivity activity = weakReference.get();
            if (activity == null) {
                return "";
            }

            try (Realm realm = Realm.getDefaultInstance()) {
                String info;
                info = activity.complexReadWrite(realm);
                info += activity.complexQuery(realm);
                return info;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            IntroExampleActivity activity = weakReference.get();
            if (activity == null) {
                return;
            }
            activity.showStatus(result);
        }

    }

    private String complexReadWrite(Realm realm) {
        String status = "\nPerforming complex Read/Write operation...";

        // 1. Add ten person in one trasaction
        /*
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm r) {
                Dog fido = r.createObject(Dog.class);
                fido.name = "fido";
                for (int i = 0; i < 10; i++) {
                    Person person = r.createObject(Person.class, i);
                    person.setName("Person no." + i);
                    person.setAge(i);
                    person.setDog(fido);

                    // The field tempReference is annotated with @Ignore.
                    // This means setTempReference sets the Person tempReference
                    // field directly. The tempReference is NOT saved as part of
                    // the RealmObject:
                    person.setTempReference(42);

                    for (int j = 0; j < i; j++) {
                        Cat cat = r.createObject(Cat.class);
                        cat.name = "Cat_" + j;
                        person.getCats().add(cat);
                    }
                }
            }
        });
        //*/
        // Add ten person in one trasaction
        //* 2. 람다식으로 표현식
        realm.executeTransaction(r->{
            Dog fido = r.createObject(Dog.class);
            fido.name = "fido";
            for (int i = 0; i < 10; i++) {
                Person person = r.createObject(Person.class, i);
                person.setName("Person no." + i);
                person.setAge(i);
                person.setDog(fido);

                // The field tempReference is annotated with @Ignore.
                // This means setTempReference sets the Person tempReference
                // field directly. The tempReference is NOT saved as part of
                // the RealmObject:
                person.setTempReference(42);

                for (int j = 0; j < i; j++) {
                    Cat cat = r.createObject(Cat.class);
                    cat.name = "Cat_" + j;
                    person.getCats().add(cat);
                }
            }
        });
        //*/
        return status;
    }

    private String complexQuery(Realm realm) {
        String status = "\n\nPerforming complex Query operation...";
        status += "\nNumber of persons: " + realm.where(Person.class).count();

        // Field all persons where age between 7 and 9 and name begins with "Person".
        RealmResults<Person> results = realm.where(Person.class)
                .between("age", 7, 9)
                .beginsWith("name", "Person").findAll();

        status += "\nSize of result set: " + results.size();

        return status;
    }




























}
