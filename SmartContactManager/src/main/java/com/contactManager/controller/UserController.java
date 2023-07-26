package com.contactManager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.contactManager.dao.ContactRepository;
import com.contactManager.dao.UserRepository;
import com.contactManager.entities.Contact;
import com.contactManager.entities.User;
import com.contactManager.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private ContactRepository contactRepo;
	
	@ModelAttribute
	public void commonData(Model model,Principal principal)
	{
		String userName = principal.getName();
		System.out.println("USERNAME: "+userName);
		User user = userRepo.getUserByEmail(userName);
		System.out.println("USER: "+user);
		model.addAttribute("user", user);
	}
	
	@RequestMapping("/index")
	public String dashboard(Model model)
	{
		model.addAttribute("title", "User Dashboard");
		return "Normal/user_dashboard";
	}
	
	@GetMapping("/add-contact")
	public String openAddContact(Model model)
	{
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") int page,Model model,Principal principal)
	{
		String name = principal.getName();
		User user = this.userRepo.getUserByEmail(name);
		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> contacts = this.contactRepo.findContactsByUser(user.getId(),pageable);
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentpage", page);
		model.addAttribute("totalpage", contacts.getTotalPages());
		model.addAttribute("title", "Show Contacts");
		return "normal/show_contacts";
	}
	
	@GetMapping("/contact/{cid}")
	public String showContactDetails(@PathVariable("cid") int id,Model model,Principal principal)
	{
		Optional<Contact> contactOptional = contactRepo.findById(id);
		Contact contact = contactOptional.get();
		String userName = principal.getName();
		User user = this.userRepo.getUserByEmail(userName);
		if(user.getId()==contact.getUser().getId())
			model.addAttribute("contact", contact);
		model.addAttribute("title", "Contact");
		return "normal/contact_details";
	}
	
	@PostMapping("/process-contact")
	public String processAddContactForm(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file 
			,Principal principal,HttpSession session)
	{
		try {
		String name = principal.getName();
		User user = this.userRepo.getUserByEmail(name);
		
		if(file.isEmpty())
		{
			
			System.out.println("Image file is empty");
			contact.setImage("contact.jpg");
			
		}else {
			contact.setImage(file.getOriginalFilename());
			File file2 = new ClassPathResource("static/img").getFile();
			Path path = Paths.get(file2.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Contact Imgage uploaded successfully");
		}
		contact.setUser(user);
		user.getContacts().add(contact);
		this.userRepo.save(user);
		System.out.println("CONTACT: "+contact);
		
		session.setAttribute("message", new Message("Contact is added successfully !!", "success"));
		
		}catch (Exception e) {
			System.out.println("Error: "+e.getMessage());
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong !!", "danger"));
		}
		return "normal/add_contact_form";
	}
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") int id, HttpSession session, Principal principal)
	{
		Optional<Contact> contactOptional = this.contactRepo.findById(id);
		Contact contact = contactOptional.get();
		User user = this.userRepo.getUserByEmail(principal.getName());
		user.getContacts().remove(contact);
		userRepo.save(user);
//		contact.setUser(null);
//		this.contactRepo.delete(contact);
		session.setAttribute("message", new Message("Contact deleted successfully...", "success"));
		return "redirect:/user/show-contacts/0";
	}
	
	@GetMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") int id,Model model)
	{
		Contact contact = this.contactRepo.findById(id).get();
		model.addAttribute("contact", contact);
		model.addAttribute("title", "Update Contact");
		return "normal/update_form";
	}
	
	@PostMapping("/update-process")
	public String updateProcess(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,
								Model model, HttpSession session1,Principal principal)
	{
		Contact oldContact= this.contactRepo.findById(contact.getCid()).get();
		
		try {
			
			if(!file.isEmpty())
			{
//				Deleting Image
				File deleteFile = new ClassPathResource("static/img").getFile();
				File oldFile = new File(deleteFile,oldContact.getImage());
				oldFile.delete();
				
//				updating images
				File file2 = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(file2.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
				
			}else {
				
				contact.setImage(oldContact.getImage());
			}
			User user = this.userRepo.getUserByEmail(principal.getName());
			contact.setUser(user);
			this.contactRepo.save(contact);
			session1.setAttribute("message", new Message("Contact updated successfully......", "success"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/user/contact/"+contact.getCid();
	}
	
	@GetMapping("/profile")
	public String showProfile(Model model)
	{
		model.addAttribute("title", "Profile View");
		return "normal/profile";
	}
	
	
	
	
	
}
