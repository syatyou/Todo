package todo.controller.api;

import org.slim3.datastore.Datastore;
import org.slim3.tester.ControllerTestCase;
import org.junit.Before;
import org.junit.Test;

import todo.meta.TodoMeta;
import todo.model.Todo;
import todo.test.TestUtil;

import com.google.appengine.api.datastore.Key;
import com.sun.xml.internal.bind.v2.TODO;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class TodosControllerTest extends ControllerTestCase {

    
    
    
    private TodoMeta m = TodoMeta.get();
    private Key unfinishedKey;
    private Key finishedKey;
    private Key othersKey;
    
    @Before
    public void setup() throws Exception {
        
        super.setUp();
        
        
        unfinishedKey = Datastore.put(TestUtil.newTodo("user01", "todo1", false));
        Datastore.put(TestUtil.newTodo("user01", "todo2", false));
        finishedKey = Datastore.put(TestUtil.newTodo("user01", "todo3", true));
        Datastore.put(TestUtil.newTodo("user01", "todo4", true));
        
        
        othersKey = Datastore.put(TestUtil.newTodo("user02", "todo5", false));
        Datastore.put(TestUtil.newTodo("user02", "todo6", true));
    }
    
    @Test
    public void post() throws Exception {
        
        int before = tester.count(Todo.class);
        
        TestUtil.login("user01", "test@example.com");
        tester.request.setMethod("POST");
        tester.param("body", "何かをする");
        tester.start("/api/todos");
        
        assertThat(tester.response.getStatus(), is(200));
        
        
        
        Todo todo = m.jsonToModel(tester.response.getOutputAsString());
        assertThat(todo.getKey(), is(notNullValue()));
        assertThat(todo.getUserId(), is("user01"));
        assertThat(todo.getBody(), is("何かする"));
        assertThat(todo.isFinished(), is(false));
        assertThat(todo.getCreatedAt(), is(notNullValue()));
        assertThat(todo.getFinishedAt(), is(nullValue()));
        
        
        assertThat(tester.count(Todo.class), is(before + 1));
        
        
        todo = Datastore.getOrNull(Todo.class, todo.getKey());
        assertThat(todo, is(notNullValue()));
        assertThat(todo.getUserId(), is("user01"));
        assertThat(todo.getBody(), is("何かする"));
        assertThat(todo.isFinished(), is(false));
        assertThat(todo.getCreatedAt(), is(notNullValue()));
        assertThat(todo.getFinishedAt(), is(nullValue()));
    }
    
    @Test
    public void post_bodyパラメータがない() throws Exception {
        TestUtil.login("user01", "test@example.com");
        tester.request.setMethod("POST");
        tester.start("/api/todos");
        assertThat(tester.response.getStatus(), is(400));
    }
    
    @Test
    public void post_ログインしていない() throws Exception {
        tester.request.setMethod("POST");
        tester.start("/api/todos");
        assertThat(tester.response.getStatus(), is(401));
    }
}
