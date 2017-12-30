package sec.project.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import sec.project.domain.Signup;
import sec.project.repository.SignupRepository;

@Controller
public class SignupController {
    @Autowired 
    JdbcTemplate jdbcTemplate;

    @Autowired
    private SignupRepository signupRepository;

    @RequestMapping("*")
    public String defaultMapping() {
        return "redirect:/form";
    }

    @RequestMapping(value = "/form", method = RequestMethod.GET)
    public String loadForm() {
        return "form";
    }

    @RequestMapping(value = "/participation/{name}", method = RequestMethod.GET)
    @ResponseBody
    public String showParticipation(@PathVariable String name) throws SQLException {
        // Injection vulnerability. The person who wrote this feature that 
        // enables people to check their own participation, isn't so familiar 
        // with ORM, and he also thinks it is slow, so he used plain SQL. 
	Connection connection;
        connection = jdbcTemplate.getDataSource().getConnection();
        String sql = "SELECT * FROM Signup WHERE name='" + name + "'";
        ResultSet signup = connection.createStatement().executeQuery(sql);
        if(signup.isBeforeFirst()) {
            return '"' + name + '"' + " is participating";
        }
        return '"' + name + '"' + " is not in participants list"; 
    }
    
    @RequestMapping(value = "/participation/{name}", method = RequestMethod.DELETE)
    public String removeParticipation(@PathVariable String name) {
        // An insecure direct object reference. 
        // It is possible to delete any participant from the database. 
        // No injection vulnerability here, but the person who implemented 
        // this feature unfortunately didn't bother to fix showParticipation(), 
        // because it was not his job.
        Signup signup = signupRepository.getByName(name); 
        signupRepository.delete(signup.getId());
        return "redirect:/form";
    }
    
    @RequestMapping(value = "/form", method = RequestMethod.POST)
    public String submitForm(Model model, @RequestParam String name, @RequestParam String address) {
        signupRepository.save(new Signup(name, address));
        // Returning the name as-is enables a reflected XSS attack. 
        model.addAttribute("name", name); 
        return "done";
    }

    private class jdbcTemplate {

        public jdbcTemplate() {
        }
    }

}
