package org.vaadin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.vaadin.alump.distributionbar.DistributionBar;
import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.BugrapRepository.ReportsQuery;
import org.vaadin.bugrap.domain.entities.Project;
import org.vaadin.bugrap.domain.entities.ProjectVersion;
import org.vaadin.bugrap.domain.entities.Report;
import org.vaadin.bugrap.domain.entities.Reporter;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class Overview extends VerticalLayout implements View {
	private static final String ALL_VERSIONS_NAME = "All Versions";
	private HashMap<String, Project> projectNames = new HashMap<String, Project>();
	private HashMap<String, HashMap<String,ProjectVersion>> projectVersions =
			new HashMap<String, HashMap<String,ProjectVersion>>();
	
	private Project currentProject=null;
	private ProjectVersion currentProjectVersion=null;
	// ==========< Back-end Binding>==========
	private BugrapUI bugrapUI;
	private BugrapRepository repository;
	private boolean loginSatus;

	private Button logout;

	// ==========< Table Filtering >==========
	private ComboBox projNameComboBox;
	private NativeSelect version;
	private DistributionBar dBar;
	private MenuBar assigneesMenu;
	private MenuItem selectedAssigneeItem;
	private Reporter assignee;
	private MenuBar statusMenu;
	private MenuItem selectedStatusItem;
	private Set<Report.Status> projectStatus;

	//
	private Grid reportTable;

	//
	private Panel editReportPanel;
	// private ButtonGroup myButtonGroup;
	public Overview(BugrapUI bugrapUI) {
		
		this.bugrapUI = bugrapUI;
		this.repository = bugrapUI.getRepository();
		this.loginSatus = false;

		// create projects and versions maps
		for (Project project : repository.findProjects()) {
			projectNames.put(project.getName(), project);
			
			for(ProjectVersion version:repository.findProjectVersions(project)) {
				if(!projectVersions.containsKey(project.getName())) {
					projectVersions.put(project.getName(), new HashMap<String,ProjectVersion>());
				}
				projectVersions.get(project.getName()).put(version.getVersion(), version);
			}
			projectVersions.get(project.getName()).put(ALL_VERSIONS_NAME, null);
		}
		
		
		// Logout button
		logout = new Button("Logout", e -> this.logout());

		// Project name
		projNameComboBox = new ComboBox("Choose project: ");
		projNameComboBox.setNullSelectionAllowed(false);
		projNameComboBox.addValueChangeListener(e -> {
			currentProject = projectNames.get(projNameComboBox.getValue());
			versionUpdate();
			dBarUpdate();
			updateTable();
		});

		// Version
		version = new NativeSelect("Reports for");
		version.setNullSelectionAllowed(false);
		version.addValueChangeListener(e -> {
			currentProjectVersion = projectVersions.get(currentProject.getName()).get(version.getValue());
			dBarUpdate();
			updateTable();
		});

		// Report distribution Bar
		dBar = new DistributionBar(3);
		dBar.setWidth("100%");

		// Assignees menu
		assigneesMenu = new MenuBar();
		assignee = null;
		selectedAssigneeItem = null;
		/**
		 * myButtonGroup = new ButtonGroup(); myButtonGroup.addButton(new
		 * Button("Left button", e -> { })); myButtonGroup.addButton(new Button(
		 * "Middle button", e -> { })); myButtonGroup.addButton(new Button(
		 * "Right button", e -> { })); Iterator<Component> mybuttons =
		 * myButtonGroup.iterator();
		 */

		// Status menu
		statusMenu = new MenuBar();
		selectedStatusItem = null;
		projectStatus = new HashSet<>();

		// Report Table
		reportTable = new Grid();

		// Edit Panel
		editReportPanel = new Panel();
		editReportPanel.setHeight("300px");
	}

	void init() {
		setSpacing(true);
		setMargin(true);
		// ==========< ProjectName, Username, Logout >==========
		addComponent(projName_Username_Logout());
		// ==========< VersionSelect and DistributionBar >==========
		addComponent(ver_And_DBar());
		// ==========< Fine Filtering >==========
		HorizontalLayout filteringMenu = new HorizontalLayout(assigneeMenu(), statusMenu());
		filteringMenu.setSpacing(true);
		addComponent(filteringMenu);
		// ==========< Report Table >==========
		addComponent(reportTable);
		// ==========< Edit Panel >==========
		addComponent(editReportPanel);
	}

	private void logout() {
		// navigator.removeView(BugrapUI.OVERVIEW_VIEW);
		VaadinSession.getCurrent().close();
		Page.getCurrent().setLocation(VaadinServlet.getCurrent().getServletContext().getContextPath() + "");
	}

	private HorizontalLayout projName_Username_Logout() {

		// Username
		Label user = new Label("Welcome " + ((Reporter) getSession().getAttribute("user")).getName() + "!");
		user.setWidth("150px");

		// Set Project name
		for (String projectName : projectNames.keySet()) {
			projNameComboBox.addItem(projectName);
		}
		projNameComboBox.setValue(projectNames.keySet().iterator().next());

		FormLayout projName = new FormLayout(projNameComboBox);
		projName.setMargin(false);

		// Form layout
		HorizontalLayout projName_Username_Logout = new HorizontalLayout(projName, user, this.logout);
		projName_Username_Logout.setExpandRatio(projName, 1);

		projName_Username_Logout.setSpacing(true);
		projName_Username_Logout.setWidth("100%");
		return projName_Username_Logout;
	}

	private HorizontalLayout ver_And_DBar() {

		FormLayout versionLayout = new FormLayout(version);
		versionLayout.setMargin(false);

		//
		HorizontalLayout ver_And_DBar = new HorizontalLayout(versionLayout, dBar);
		ver_And_DBar.setExpandRatio(dBar, 3);
		ver_And_DBar.setExpandRatio(versionLayout, 1);
		ver_And_DBar.setSizeFull();
		ver_And_DBar.setSpacing(true);
		return ver_And_DBar;
	}

	private MenuBar assigneeMenu() {
		assigneesMenu.setCaption("Assignees");
		assigneesMenu.addStyleName("mybarmenu");

		MenuItem onlyMe = assigneesMenu.addItem("Only me", null, null);
		MenuItem everyone = assigneesMenu.addItem("Everyone", null, null);

		// Set Default
		everyone.setStyleName("highlight");
		selectedAssigneeItem = everyone;

		// assignees top level menu items
		onlyMe.setCommand(new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				selectedAssigneeItem.setStyleName(null);
				selectedItem.setStyleName("highlight");
				selectedAssigneeItem = selectedItem;
				assignee = (Reporter) getSession().getAttribute("user");
				updateTable();
			}
		});

		everyone.setCommand(new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				selectedAssigneeItem.setStyleName(null);
				selectedItem.setStyleName("highlight");
				selectedAssigneeItem = selectedItem;
				assignee = null;
				updateTable();
			}
		});

		return assigneesMenu;
	}

	private MenuBar statusMenu() {
		statusMenu.setCaption("Status");
		statusMenu.addStyleName("mybarmenu");

		// Add top level menu items
		MenuItem openStatus = statusMenu.addItem("Open", null, null);
		MenuItem allKindsStatus = statusMenu.addItem("All kinds", null, null);
		MenuItem customStatus = statusMenu.addItem("Custom", null, null);

		// set all kinds default selected status
		allKindsStatus.setStyleName("highlight");
		selectedStatusItem = allKindsStatus;

		// Add custom menu items
		MenuItem open = customStatus.addItem("Open", null, null);
		open.setCheckable(true);
		MenuItem fixed = customStatus.addItem("Fixed", null, null);
		fixed.setCheckable(true);
		MenuItem invalid = customStatus.addItem("Invalid", null, null);
		invalid.setCheckable(true);
		MenuItem wontFix = customStatus.addItem("Wonʼt fix", null, null);
		wontFix.setCheckable(true);
		MenuItem cantFix = customStatus.addItem("Canʼt fix", null, null);
		cantFix.setCheckable(true);
		MenuItem duplicate = customStatus.addItem("Duplicate", null, null);
		duplicate.setCheckable(true);
		MenuItem worksforMe = customStatus.addItem("Works for me", null, null);
		worksforMe.setCheckable(true);
		MenuItem needsMoreInfo = customStatus.addItem("Needs more information", null, null);
		needsMoreInfo.setCheckable(true);

		// set commands
		openStatus.setCommand(new MenuBar.Command() {

			@Override
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
				if (selectedStatusItem != null)
					selectedStatusItem.setStyleName(null);
				selectedItem.setStyleName("highlight");
				selectedStatusItem = selectedItem;
				projectStatus.clear();
				for (MenuItem i : customStatus.getChildren()) {
					i.setChecked(false);
				}
				projectStatus.add(Report.Status.OPEN);
				open.setChecked(true);
				updateTable();
			}
		});
		allKindsStatus.setCommand(new MenuBar.Command() {

			@Override
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
				if (selectedStatusItem != null)
					selectedStatusItem.setStyleName(null);
				selectedItem.setStyleName("highlight");
				selectedStatusItem = selectedItem;
				projectStatus.clear();
				for (MenuItem i : customStatus.getChildren()) {
					i.setChecked(false);
				}
				updateTable();
			}
		});

		// New MenuBar Command class
		class customizeFilteringCommand implements MenuBar.Command {
			protected Report.Status slectedStat;

			public customizeFilteringCommand(Report.Status slectedStat) {
				this.slectedStat = slectedStat;
			}

			@Override
			public void menuSelected(MenuItem selectedItem) {
				// TODO Auto-generated method stub
				if (selectedStatusItem != null)
					selectedStatusItem.setStyleName(null);
				customStatus.setStyleName("highlight");
				selectedStatusItem = customStatus;
				if (selectedItem.isChecked()) {
					projectStatus.add(slectedStat);
				} else {
					projectStatus.remove(slectedStat);
					if (projectStatus.isEmpty()) {
						customStatus.setStyleName("mybarmenu");
						allKindsStatus.setStyleName("highlight");
						selectedStatusItem = allKindsStatus;
					}
				}
				updateTable();
			}
		}

		open.setCommand(new customizeFilteringCommand(Report.Status.OPEN));
		fixed.setCommand(new customizeFilteringCommand(Report.Status.FIXED));
		invalid.setCommand(new customizeFilteringCommand(Report.Status.INVALID));
		wontFix.setCommand(new customizeFilteringCommand(Report.Status.WONT_FIX));
		cantFix.setCommand(new customizeFilteringCommand(Report.Status.CANT_FIX));
		duplicate.setCommand(new customizeFilteringCommand(Report.Status.DUPLICATE));
		worksforMe.setCommand(new customizeFilteringCommand(Report.Status.WORKS_FOR_ME));
		needsMoreInfo.setCommand(new customizeFilteringCommand(Report.Status.NEED_MORE_INFO));

		return statusMenu;
	}

	// add values from Repository to map
	// add null value with hash code "all versions"

	private void versionUpdate() {
		version.removeAllItems();
		for (String versionName : projectVersions.get(currentProject.getName()).keySet()) {
			version.addItem(versionName);
		}
		version.select(ALL_VERSIONS_NAME);
	}

	private void dBarUpdate() {

		if (currentProjectVersion == null) {
			long nProjects=repository.countClosedReports(currentProject);
			dBar.setPartSize(0, nProjects, Long.toString(nProjects));
			
			nProjects=repository.countOpenedReports(currentProject);
			dBar.setPartSize(1,nProjects,Long.toString(nProjects));

			nProjects=repository.countUnassignedReports(currentProject);
			dBar.setPartSize(2, nProjects, Long.toString(nProjects));
		} else {
			long nVersions=repository.countClosedReports(currentProjectVersion);
			dBar.setPartSize(0, nVersions, Long.toString(nVersions));
			
			nVersions=repository.countOpenedReports(currentProjectVersion);
			dBar.setPartSize(1,nVersions,Long.toString(nVersions));

			nVersions=repository.countUnassignedReports(currentProjectVersion);
			dBar.setPartSize(2, nVersions, Long.toString(nVersions));
		}
	}

	private void updateTable() {
		reportTable.setSizeFull();
		ReportsQuery query = new ReportsQuery();
		query.project = currentProject;
		query.projectVersion = currentProjectVersion;
		query.reportStatuses = null;
		query.reportAssignee = assignee;

		Set<Report> reports = new HashSet<>();
		if (projectStatus.isEmpty()) {
			reports = repository.findReports(query);
		}
		for (Report.Status i : projectStatus) {
			query.reportStatuses = new HashSet<Report.Status>();
			query.reportStatuses.add(i);
			reports.addAll(repository.findReports(query));
			query.reportStatuses.remove(i);
		}
		reportTable.setContainerDataSource(new BeanItemContainer<>(Report.class, reports));
		if (version.getValue() == ALL_VERSIONS_NAME) {
			reportTable.setColumns("version", "priority", "type", "summary", "assigned", "timestamp",
					"reportedTimestamp");
		} else {
			reportTable.setColumns("priority", "type", "summary", "assigned", "timestamp", "reportedTimestamp");
		}
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub

		Reporter user = (Reporter) getSession().getAttribute("user");
		if (user != null) {
			if (!loginSatus) {
				loginSatus = true;
				init();
			}
		} else {
			Page.getCurrent().setLocation(VaadinServlet.getCurrent().getServletContext().getContextPath());
		}
	}

}
