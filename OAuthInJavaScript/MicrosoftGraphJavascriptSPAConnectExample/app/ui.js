// Select DOM elements to work with
const welcomeDiv = document.getElementById("WelcomeMessage");
const signInButton = document.getElementById("SignIn");
const cardDiv = document.getElementById("card-div");
const mailButton = document.getElementById("readMail");
const profileButton = document.getElementById("seeProfile");
const profileDiv = document.getElementById("profile-div");

function showWelcomeMessage(account) {
    // Reconfiguring DOM elements
    cardDiv.style.display = 'initial';
    welcomeDiv.innerHTML = `Welcome ${account.username}`;
    signInButton.setAttribute("onclick", "signOut();");
    signInButton.setAttribute('class', "btn btn-success")
    signInButton.innerHTML = "Sign Out";
}

function updateUIWithProfileInfo(data) {
	profileButton.style.display = 'none';

	const id = document.createElement('p');
	id.innerHtml = "<strong>Id: </strong>" + data.id;
	profileDiv.appendChild(id);

	const email = document.createElement('p');
	email.innerHTML = "<strong>Mail address: </strong>" + data.mail;
	profileDiv.appendChild(email);

	if (Array.isArray(data.businessPhones) && data.businessPhones.length > 0)
	{
		const phone = document.createElement('p');
		phone.innerHTML = "<strong>Phone: </strong>" + data.businessPhones[0];
		profileDiv.appendChild(phone);
	}
}

function updateUIWithMessages(data) {
	mailButton.style.display = 'none';
	if (data.value.length < 1) {
		alert("Your mailbox is empty!")
	} else {
		const tabList = document.getElementById("list-tab");
		const tabContent = document.getElementById("nav-tabContent");

		data.value.map((d, i) => {
			// Keeping it simple
			if (i < 10) {
				const listItem = document.createElement("a");
				listItem.setAttribute("class", "list-group-item list-group-item-action")
				listItem.setAttribute("id", "list" + i + "list")
				listItem.setAttribute("data-toggle", "list")
				listItem.setAttribute("href", "#list" + i)
				listItem.setAttribute("role", "tab")
				listItem.setAttribute("aria-controls", i)
				listItem.innerHTML = d.subject;
				tabList.appendChild(listItem)

				const contentItem = document.createElement("div");
				contentItem.setAttribute("class", "tab-pane fade")
				contentItem.setAttribute("id", "list" + i)
				contentItem.setAttribute("role", "tabpanel")
				contentItem.setAttribute("aria-labelledby", "list" + i + "list")
				contentItem.innerHTML = "<strong> from: " + d.from.emailAddress.address + "</strong><br><br>" + d.bodyPreview + "...";
				tabContent.appendChild(contentItem);
			}
		});
	}
}
