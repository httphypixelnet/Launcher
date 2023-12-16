const socket = new WebSocket("ws://localhost:4444");
//version is going to be whatever version is selected from the dropdown
let version = "1.8.9";
// remove the comments from this when you can actually change version
// let version;
let launchmessage = "Launching Minecraft version " + version;
let selectedAccount = "";
let accounts;

socket.onmessage = function (event) {
  if (event.data.startsWith("signIn=")) {
    const listItems = document.querySelectorAll("#accDrop li");
    for (let i = 0; i < listItems.length; i++) {
      const item = listItems[i];
      if (item.textContent.startsWith(event.data.replace("signIn=", ""))) {
        return;
      }
    }

    const div = document.createElement("div")
    div.setAttribute("id",event.data.replace("signIn=", ""));
    const listItem = document.createElement("li");

    const link = document.createElement("a");
    link.className = "dropdown-item z-5"
    link.style.cursor = "pointer";
    link.setAttribute("onclick", `selectedAccount="${event.data.replace("signIn=", "")}";updateAcc();updateAcc();/*socket.send("selectedAccount="${event.data.replace("signIn=", "")}");*/`);
    link.textContent = event.data.replace("signIn=", "");

    const closeButton = document.createElement("button");
    closeButton.className = "btn btn-sm btn-danger btn-octicon";
    closeButton.style.position = "absolute"
    closeButton.type = "button";
    closeButton.style.fill = "white";
    closeButton.style.verticalAlign = "middle";
    closeButton.style.float = "right";
    closeButton.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="3 3 10 10" width="10" height="10"><path d="M3.72 3.72a.75.75 0 0 1 1.06 0L8 6.94l3.22-3.22a.749.749 0 0 1 1.275.326.749.749 0 0 1-.215.734L9.06 8l3.22 3.22a.749.749 0 0 1-.326 1.275.749.749 0 0 1-.734-.215L8 9.06l-3.22 3.22a.751.751 0 0 1-1.042-.018.751.751 0 0 1-.018-1.042L6.94 8 3.72 4.78a.75.75 0 0 1 0-1.06Z"></path></svg>`;

    closeButton.addEventListener("click", function () {
      listItem.remove();
      socket.send("signOut=" + event.data.replace("signIn=", ""));
    });

    link.appendChild(closeButton);
    listItem.appendChild(link);
    div.appendChild(listItem);
    document.getElementById("accDrop").insertBefore(div, document.getElementById("accDrop").firstChild);
  } else if (event.data.startsWith("accounts=")) {

    accounts = JSON.parse(event.data.replace("accounts=", ""));
    for (let i = 0; i < accounts.length; i++) {
      const listItems = document.querySelectorAll("#accDrop li");
      for (let a = 0; a < listItems.length; a++) {
        const item = listItems[a];
        if (item.textContent.startsWith(accounts[i])) {
          return;
        }
      }

      const div = document.createElement("div")
      div.setAttribute("id", accounts[i]);

      const listItem = document.createElement("li");

      const link = document.createElement("a");
      link.setAttribute("class", "dropdown-item z-5");
      link.style.cursor = "pointer";
      link.setAttribute("onclick", `selectedAccount="${accounts[i]}";updateAcc();/*socket.send("selectedAccount="${accounts[i]}");*/`);
      link.textContent = accounts[i];

      const closeButton = document.createElement("button");
      closeButton.className = "btn btn-sm btn-danger btn-octicon";
      closeButton.type = "button";
      closeButton.style.fill = "white";
      closeButton.style.verticalAlign = "middle";
      closeButton.style.float = "right";
      closeButton.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="3 3 10 10" width="10" height="10"><path d="M3.72 3.72a.75.75 0 0 1 1.06 0L8 6.94l3.22-3.22a.749.749 0 0 1 1.275.326.749.749 0 0 1-.215.734L9.06 8l3.22 3.22a.749.749 0 0 1-.326 1.275.749.749 0 0 1-.734-.215L8 9.06l-3.22 3.22a.751.751 0 0 1-1.042-.018.751.751 0 0 1-.018-1.042L6.94 8 3.72 4.78a.75.75 0 0 1 0-1.06Z"></path></svg>`;

      closeButton.addEventListener("click", function () {
        listItem.remove();
        socket.send("signOut=" + accounts[i]);
      });

      link.appendChild(closeButton);
      listItem.appendChild(link);
      div.appendChild(listItem);
      document.getElementById("accDrop").insertBefore(div, document.getElementById("accDrop").firstChild);
    }
    selectedAccount = accounts[0]
  }
}

function updateAcc() {
  let accountDropdown = document.getElementById("accDrop");
  let c = Array.from(accountDropdown.children);
  c.slice(0,c.length).forEach((child) => {
    if (selectedAccount === child.textContent) {
      child.style.background = "#16191d";
    } else {
      child.style.background = "";
    }
  });
}

socket.onopen = function () {
  socket.send("getAccountList");
}