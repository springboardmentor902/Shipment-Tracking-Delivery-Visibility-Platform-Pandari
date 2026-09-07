/* =========================================================
   ShipTrack Pro Frontend
   HTML + CSS + JavaScript + Leaflet + STOMP/SockJS
   ========================================================= */

const CONFIG = {
  API_BASE_URL: "http://localhost:8081",
  WEBSOCKET_ENDPOINT: "/ws",
  LOCATION_TOPIC_PREFIX: "/topic/routes",

  // If your JWT is stored under another localStorage key, change this.
  TOKEN_STORAGE_KEY: "token",

  // This is the route GET endpoint used when a routeId is available.
  // Change only this path if your backend uses a different GET endpoint.
  ROUTE_ENDPOINT: routeId => `/api/routes/${routeId}`,

  // Used only if the backend response does not already contain destination coordinates.
  NOMINATIM_URL: "https://nominatim.openstreetmap.org/search",
  OSRM_URL: "https://router.project-osrm.org/route/v1/driving"
};

const state = {
  shipments: [
    {
      id:"ST-2026-1042", routeId:1,
      destination:"Hyderabad, Telangana", created:"18 Aug 2026", status:"In Transit",
      receiver:"Anil Kumar", phone:"9876501234", address:"Madhapur, Hyderabad", eta:"21 Aug 2026",
      timeline:[
        ["Shipment Created","18 Aug 2026, 09:20 AM","Shipment has been registered."],
        ["Picked Up","18 Aug 2026, 12:10 PM","Package picked up from sender."],
        ["In Transit","18 Aug 2026, 04:45 PM","Package is moving to the destination hub."]
      ]
    },
    {
      id:"ST-2026-1038", routeId:null,
      destination:"Vijayawada, Andhra Pradesh", created:"16 Aug 2026", status:"Out for Delivery",
      receiver:"Priya Reddy", phone:"9988776655", address:"Benz Circle, Vijayawada", eta:"18 Aug 2026",
      timeline:[
        ["Shipment Created","16 Aug 2026, 10:15 AM","Shipment registered."],
        ["In Transit","17 Aug 2026, 07:40 PM","Reached Vijayawada hub."],
        ["Out for Delivery","18 Aug 2026, 08:20 AM","Courier is out for delivery."]
      ]
    },
    {
      id:"ST-2026-1029", routeId:null,
      destination:"Warangal, Telangana", created:"12 Aug 2026", status:"Delivered",
      receiver:"Suresh Rao", phone:"9000012345", address:"Hanamkonda, Warangal", eta:"15 Aug 2026",
      timeline:[
        ["Shipment Created","12 Aug 2026, 11:00 AM","Shipment registered."],
        ["In Transit","13 Aug 2026, 03:30 PM","Shipment reached destination city."],
        ["Delivered","15 Aug 2026, 02:10 PM","Package delivered successfully."]
      ]
    },
    {
      id:"ST-2026-1017", routeId:null,
      destination:"Chennai, Tamil Nadu", created:"10 Aug 2026", status:"Cancelled",
      receiver:"Kiran Das", phone:"9111122222", address:"T Nagar, Chennai", eta:"—",
      timeline:[
        ["Shipment Created","10 Aug 2026, 09:00 AM","Shipment registered."],
        ["Cancelled","11 Aug 2026, 01:30 PM","Shipment cancelled by customer."]
      ]
    }
  ],
  users: [
    {name:"Ravi Kumar",email:"ravi.kiran@example.com",role:"BUSINESS_CLIENT",status:"ACTIVE"},
    {name:"Admin User",email:"admin@shiptrack.com",role:"ADMINISTRATOR",status:"ACTIVE"},
    {name:"Meena Sharma",email:"meena@example.com",role:"CUSTOMER",status:"ACTIVE"},
    {name:"Arjun Rao",email:"arjun@example.com",role:"OPERATOR",status:"INACTIVE"}
  ]
};

const sections = ["dashboard","profile","business","settings","admin","shipments","create-shipment","details"];

// =========================================================
// LIVE TRACKING STATE
// =========================================================
let stompClient = null;
let currentSubscription = null;
let currentRouteId = null;
let currentShipmentId = null;
let trackingMap = null;
let vehicleMarker = null;
let routeLine = null;
let destinationMarker = null;
let lastVehicleLocation = null;
let lastRouteDestination = null;
let routeLoadController = null;

// =========================================================
// NAVIGATION
// =========================================================

document.querySelectorAll(".nav-item").forEach(btn => {
  btn.addEventListener("click", () => navigate(btn.dataset.section));
});

function navigate(section){
  // Leaving shipment details must close the live tracking session.
  if (section !== "details") {
    disconnectTracking();
  }

  sections.forEach(s => document.getElementById(s)?.classList.remove("active"));
  document.getElementById(section)?.classList.add("active");
  document.querySelectorAll(".nav-item").forEach(b => b.classList.toggle("active", b.dataset.section === section));

  const titles = {
    dashboard:"Dashboard", profile:"Profile", business:"Business Account",
    settings:"Account Settings", admin:"Manage Users", shipments:"My Shipments",
    "create-shipment":"Create Shipment", details:"Shipment Details"
  };

  document.getElementById("pageTitle").textContent = titles[section] || "Dashboard";
  if(section === "shipments") renderShipments();
  if(section === "admin") renderUsers();
  if(section === "dashboard") renderRecent();
  window.scrollTo({top:0,behavior:"smooth"});
}

// =========================================================
// SHIPMENT LIST / DETAILS
// =========================================================

function statusClass(status){
  if(status === "Delivered") return "done";
  if(status === "Out for Delivery") return "out";
  if(status === "Cancelled") return "cancel";
  return "in";
}

function statusBadge(status){
  return `<span class="status ${statusClass(status)}">${status}</span>`;
}

function renderRecent(){
  const el = document.getElementById("recentShipments");
  el.innerHTML = state.shipments.slice(0,4).map(s => `
    <div class="shipment-row">
      <div><div class="tracking">${s.id}</div><div class="destination">${s.destination}</div></div>
      <div>${statusBadge(s.status)}</div>
    </div>`).join("");
}

function renderShipments(){
  const search = document.getElementById("shipmentSearch")?.value.toLowerCase() || "";
  const filter = document.getElementById("shipmentFilter")?.value || "";
  const rows = state.shipments.filter(s =>
    s.id.toLowerCase().includes(search) && (!filter || s.status === filter)
  );

  document.getElementById("shipmentsTable").innerHTML = rows.length ? rows.map(s => `
    <tr>
      <td><strong>${s.id}</strong></td>
      <td>${s.destination}</td>
      <td>${s.created}</td>
      <td>${statusBadge(s.status)}</td>
      <td>
        <button class="action-link" onclick="viewShipment('${s.id}')">View</button>
        ${s.status !== "Cancelled" && s.status !== "Delivered" ? `<button class="action-link" onclick="cancelShipment('${s.id}')">Cancel</button>` : ""}
      </td>
    </tr>`).join("") : `<tr><td colspan="5">No shipments found.</td></tr>`;
}

async function viewShipment(id){
  const s = state.shipments.find(x => x.id === id);
  if(!s) return;

  // Make sure any old shipment tracking connection is gone first.
  disconnectTracking();
  currentShipmentId = s.id;

  document.getElementById("detailSubtitle").textContent = `${s.id} • ${s.destination}`;
  document.getElementById("shipmentDetails").innerHTML = `
    <div class="details-grid">
      <div class="detail-card">
        <h4>Shipment Information</h4>
        <div class="detail-item"><span>Tracking Number</span><strong>${s.id}</strong></div>
        <div class="detail-item"><span>Status</span><strong>${statusBadge(s.status)}</strong></div>
        <div class="detail-item"><span>Receiver</span><strong>${s.receiver}</strong></div>
        <div class="detail-item"><span>Phone</span><strong>${s.phone}</strong></div>
        <div class="detail-item"><span>Destination</span><strong>${s.address}</strong></div>
        <div class="detail-item"><span>Estimated Delivery</span><strong>${s.eta}</strong></div>
        <div class="detail-item"><span>Route ID</span><strong>${s.routeId ?? "Not assigned"}</strong></div>
        ${s.status !== "Delivered" && s.status !== "Cancelled" ? `<button class="danger" style="margin-top:18px" onclick="cancelShipment('${s.id}')">Cancel Shipment</button>` : ""}
      </div>
      <div class="detail-card">
        <h4>Tracking Timeline</h4>
        <div class="timeline">
          ${s.timeline.map(t => `<div class="timeline-item"><span class="timeline-dot"></span><strong>${t[0]}</strong><p>${t[1]}</p><p>${t[2]}</p></div>`).join("")}
        </div>
      </div>
    </div>`;

  navigate("details");

  // Create the map first, then connect the WebSocket.
  initializeTrackingMap();
  setTrackingStatus("connecting", "Connecting to live tracking…");

  if (!s.routeId) {
    setTrackingStatus("offline", "No routeId is assigned to this shipment yet.");
    showToast("This shipment has no route assigned");
    return;
  }

  currentRouteId = Number(s.routeId);
  document.getElementById("routeInfo").textContent = `Route ID: ${currentRouteId}`;

  // Load route information in the background. The WebSocket can still connect.
  await loadRouteInformation(currentRouteId, s);
  connectWebSocket(currentRouteId);
}

function cancelShipment(id){
  const s = state.shipments.find(x => x.id === id);
  if(!s || s.status === "Delivered") return;
  if(confirm(`Cancel shipment ${id}?`)){
    s.status = "Cancelled";
    s.timeline.push(["Cancelled", new Date().toLocaleString(), "Shipment cancelled by customer."]);
    showToast("Shipment cancelled");
    renderShipments();
    renderRecent();
    viewShipment(id);
  }
}

// =========================================================
// MAP
// =========================================================

function initializeTrackingMap(initialLat = 17.3850, initialLng = 78.4867){
  const mapElement = document.getElementById("trackingMap");
  if(!mapElement || typeof L === "undefined") return;

  if(trackingMap){
    trackingMap.remove();
    trackingMap = null;
  }

  trackingMap = L.map("trackingMap").setView([initialLat, initialLng], 10);

  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    attribution: "&copy; OpenStreetMap contributors"
  }).addTo(trackingMap);

  // Temporary center marker. It is replaced/moved when the first WebSocket update arrives.
  vehicleMarker = L.marker([initialLat, initialLng]).addTo(trackingMap);
  vehicleMarker.bindPopup("Waiting for live vehicle location…");

  routeLine = L.polyline([], { weight: 5 }).addTo(trackingMap);

  setTimeout(() => trackingMap?.invalidateSize(), 150);
}

function updateVehicleLocation(location){
  const latitude = Number(location.latitude);
  const longitude = Number(location.longitude);

  if(!Number.isFinite(latitude) || !Number.isFinite(longitude)){
    console.warn("Invalid location received", location);
    return;
  }

  lastVehicleLocation = { latitude, longitude };

  if(!trackingMap){
    initializeTrackingMap(latitude, longitude);
  }

  if(!vehicleMarker){
    vehicleMarker = L.marker([latitude, longitude]).addTo(trackingMap);
  } else {
    vehicleMarker.setLatLng([latitude, longitude]);
  }

  vehicleMarker.bindPopup(`Live location<br>${latitude.toFixed(5)}, ${longitude.toFixed(5)}`);

  trackingMap.panTo([latitude, longitude], { animate: true, duration: 0.5 });

  document.getElementById("lastLocation").textContent =
    `Updated: ${new Date().toLocaleTimeString()} • ${latitude.toFixed(5)}, ${longitude.toFixed(5)}`;

  setTrackingStatus("connected", "Live location connected");

  // If we already know the destination, redraw the road route.
  if(lastRouteDestination){
    drawRoadRoute(latitude, longitude, lastRouteDestination.latitude, lastRouteDestination.longitude);
  }
}

function setDestinationMarker(latitude, longitude){
  if(!trackingMap) return;

  if(destinationMarker){
    destinationMarker.setLatLng([latitude, longitude]);
  } else {
    destinationMarker = L.marker([latitude, longitude]).addTo(trackingMap);
  }

  destinationMarker.bindPopup("Shipment destination");
}

// =========================================================
// WEBSOCKET / STOMP
// =========================================================

function getJwtToken(){
  return localStorage.getItem(CONFIG.TOKEN_STORAGE_KEY) ||
         localStorage.getItem("accessToken") ||
         localStorage.getItem("jwt") ||
         "";
}

function connectWebSocket(routeId){
  if(!routeId) return;

  disconnectWebSocketOnly();

  if(typeof SockJS === "undefined" || typeof StompJs === "undefined"){
    setTrackingStatus("error", "WebSocket libraries did not load. Check internet/CDN access.");
    return;
  }

  const socketUrl = `${CONFIG.API_BASE_URL}${CONFIG.WEBSOCKET_ENDPOINT}`;
  const token = getJwtToken();

  const socket = new SockJS(socketUrl);

  stompClient = new StompJs.Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,
    connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
    debug: message => console.debug("[STOMP]", message)
  });

  stompClient.onConnect = () => {
    console.log(`WebSocket connected. Subscribing to route ${routeId}`);
    setTrackingStatus("connected", `Connected • waiting for route ${routeId} location updates`);
    subscribeToRoute(routeId);
  };

  stompClient.onStompError = frame => {
    console.error("STOMP error", frame.headers, frame.body);
    setTrackingStatus("error", "WebSocket/STOMP error. Check backend logs and JWT.");
  };

  stompClient.onWebSocketError = event => {
    console.error("WebSocket error", event);
    setTrackingStatus("error", "Could not connect to WebSocket /ws.");
  };

  stompClient.onWebSocketClose = () => {
    if(currentRouteId){
      setTrackingStatus("offline", "WebSocket connection closed");
    }
  };

  stompClient.activate();
}

function subscribeToRoute(routeId){
  if(!stompClient || !stompClient.connected) return;

  if(currentSubscription){
    currentSubscription.unsubscribe();
    currentSubscription = null;
  }

  const topic = `${CONFIG.LOCATION_TOPIC_PREFIX}/${routeId}`;

  currentSubscription = stompClient.subscribe(topic, message => {
    try {
      const location = JSON.parse(message.body);
      console.log("Live location received:", location);

      // Ignore a message for another route if backend broadcasts broadly.
      if(location.routeId != null && Number(location.routeId) !== Number(routeId)) return;

      updateVehicleLocation(location);
    } catch(error){
      console.error("Could not parse WebSocket message", error, message.body);
    }
  });

  console.log(`Subscribed to ${topic}`);
}

function disconnectWebSocketOnly(){
  if(currentSubscription){
    try { currentSubscription.unsubscribe(); } catch(error) { console.warn(error); }
    currentSubscription = null;
  }

  if(stompClient){
    try { stompClient.deactivate(); } catch(error) { console.warn(error); }
    stompClient = null;
  }
}

function disconnectTracking(){
  disconnectWebSocketOnly();

  currentRouteId = null;
  currentShipmentId = null;
  lastVehicleLocation = null;
  lastRouteDestination = null;

  if(routeLoadController){
    routeLoadController.abort();
    routeLoadController = null;
  }

  if(trackingMap){
    trackingMap.remove();
    trackingMap = null;
  }

  vehicleMarker = null;
  destinationMarker = null;
  routeLine = null;

  const routeInfo = document.getElementById("routeInfo");
  if(routeInfo) routeInfo.textContent = "Route ID: —";

  const lastLocation = document.getElementById("lastLocation");
  if(lastLocation) lastLocation.textContent = "Waiting for location…";

  const map = document.getElementById("trackingMap");
  if(map) map.innerHTML = "";

  setTrackingStatus("offline", "Open a shipment to start live tracking.");
}

function setTrackingStatus(type, message){
  const el = document.getElementById("trackingStatus");
  const text = document.getElementById("trackingStatusText");
  if(!el || !text) return;

  el.className = `tracking-status ${type}`;
  const labels = {
    connected: "Live",
    connecting: "Connecting",
    offline: "Offline",
    error: "Error"
  };
  const strong = el.querySelector("strong");
  if(strong) strong.textContent = labels[type] || "Tracking";
  text.textContent = message;
}

// =========================================================
// ROUTE INFORMATION + DESTINATION + OSRM
// =========================================================

async function loadRouteInformation(routeId, shipment){
  if(!routeId) return;

  try {
    if(routeLoadController) routeLoadController.abort();
    routeLoadController = new AbortController();

    const headers = getAuthHeaders();
    const response = await fetch(`${CONFIG.API_BASE_URL}${CONFIG.ROUTE_ENDPOINT(routeId)}`, {
      headers,
      signal: routeLoadController.signal
    });

    if(!response.ok){
      console.warn(`Route GET returned ${response.status}. Trying shipment destination geocoding instead.`);
      await prepareDestinationFromShipment(shipment);
      return;
    }

    const route = await response.json();
    console.log("Route information:", route);

    // Support common backend field names without forcing one exact DTO shape.
    const destinationText = route.destination || shipment.address || shipment.destination;
    const destinationCoordinates = extractCoordinates(route, "destination");

    if(destinationCoordinates){
      setDestination(destinationCoordinates.latitude, destinationCoordinates.longitude);
      return;
    }

    if(destinationText){
      await geocodeDestination(destinationText);
    }
  } catch(error){
    if(error.name === "AbortError") return;
    console.warn("Could not load route information", error);
    await prepareDestinationFromShipment(shipment);
  }
}

async function prepareDestinationFromShipment(shipment){
  const destination = shipment.address || shipment.destination;
  if(destination) await geocodeDestination(destination);
}

function extractCoordinates(object, type){
  const candidates = type === "destination"
    ? [
        object.destinationCoordinates,
        object.destinationCoordinate,
        object.destination_coordinates,
        object.destinationLocation,
        object.destination_location
      ]
    : [object.currentLocation, object.current_location, object.currentCoordinates];

  for(const candidate of candidates){
    const coords = normalizeCoordinates(candidate);
    if(coords) return coords;
  }

  // Some DTOs may directly expose destinationLatitude/destinationLongitude.
  const latKeys = type === "destination"
    ? ["destinationLatitude","destinationLat"]
    : ["latitude","currentLatitude"];
  const lngKeys = type === "destination"
    ? ["destinationLongitude","destinationLng","destinationLon"]
    : ["longitude","currentLongitude"];

  for(const latKey of latKeys){
    for(const lngKey of lngKeys){
      const lat = Number(object[latKey]);
      const lng = Number(object[lngKey]);
      if(Number.isFinite(lat) && Number.isFinite(lng)) return {latitude:lat, longitude:lng};
    }
  }

  return null;
}

function normalizeCoordinates(value){
  if(!value) return null;

  if(Array.isArray(value) && value.length >= 2){
    const latitude = Number(value[0]);
    const longitude = Number(value[1]);
    if(Number.isFinite(latitude) && Number.isFinite(longitude)) return {latitude, longitude};
  }

  if(typeof value === "string"){
    try {
      return normalizeCoordinates(JSON.parse(value));
    } catch(_) {
      const parts = value.split(",").map(Number);
      if(parts.length >= 2 && parts.every(Number.isFinite)) return {latitude:parts[0], longitude:parts[1]};
    }
  }

  if(typeof value === "object"){
    const latitude = Number(value.latitude ?? value.lat);
    const longitude = Number(value.longitude ?? value.lng ?? value.lon);
    if(Number.isFinite(latitude) && Number.isFinite(longitude)) return {latitude, longitude};
  }

  return null;
}

async function geocodeDestination(address){
  try {
    const url = new URL(CONFIG.NOMINATIM_URL);
    url.searchParams.set("q", address);
    url.searchParams.set("format", "jsonv2");
    url.searchParams.set("limit", "1");

    const response = await fetch(url.toString(), {
      headers: { Accept: "application/json" }
    });

    if(!response.ok) throw new Error(`Nominatim returned ${response.status}`);

    const results = await response.json();
    if(!results.length){
      console.warn("Destination could not be geocoded:", address);
      return;
    }

    const latitude = Number(results[0].lat);
    const longitude = Number(results[0].lon);
    setDestination(latitude, longitude);
  } catch(error){
    console.warn("Destination geocoding failed", error);
  }
}

function setDestination(latitude, longitude){
  if(!Number.isFinite(latitude) || !Number.isFinite(longitude)) return;

  lastRouteDestination = {latitude, longitude};
  setDestinationMarker(latitude, longitude);

  if(lastVehicleLocation){
    drawRoadRoute(
      lastVehicleLocation.latitude,
      lastVehicleLocation.longitude,
      latitude,
      longitude
    );
  } else if(trackingMap){
    trackingMap.setView([latitude, longitude], 10);
  }
}

async function drawRoadRoute(startLat, startLng, endLat, endLng){
  if(!trackingMap || !routeLine) return;

  try {
    const coordinates = `${startLng},${startLat};${endLng},${endLat}`;
    const url = `${CONFIG.OSRM_URL}/${coordinates}?overview=full&geometries=geojson`;

    const response = await fetch(url);
    if(!response.ok) throw new Error(`OSRM returned ${response.status}`);

    const data = await response.json();
    const geometry = data.routes?.[0]?.geometry?.coordinates;

    if(!geometry?.length) return;

    const latLngs = geometry.map(point => [point[1], point[0]]);
    routeLine.setLatLngs(latLngs);

    const bounds = L.latLngBounds(latLngs);
    if(bounds.isValid()) trackingMap.fitBounds(bounds, {padding:[35,35]});
  } catch(error){
    console.warn("OSRM route drawing failed", error);

    // Fallback: straight line from current location to destination.
    routeLine.setLatLngs([
      [startLat, startLng],
      [endLat, endLng]
    ]);
  }
}

function getAuthHeaders(){
  const token = getJwtToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

// =========================================================
// ADMIN / PROFILE / FORMS
// =========================================================

function renderUsers(){
  const search = document.getElementById("userSearch")?.value.toLowerCase() || "";
  const filter = document.getElementById("roleFilter")?.value || "";
  const rows = state.users.filter(u =>
    (`${u.name} ${u.email}`).toLowerCase().includes(search) && (!filter || u.role === filter)
  );

  document.getElementById("usersTable").innerHTML = rows.map((u,i) => `
    <tr><td><strong>${u.name}</strong></td><td>${u.email}</td><td>${u.role}</td><td>${u.status}</td><td><button class="action-link" onclick="changeRole(${i})">Change Role</button></td></tr>
  `).join("");
}

function changeRole(index){
  const u = state.users[index];
  const role = prompt(`Enter new role for ${u.name}: CUSTOMER, BUSINESS_CLIENT, OPERATOR or ADMINISTRATOR`, u.role);
  if(!role) return;
  const allowed = ["CUSTOMER","BUSINESS_CLIENT","OPERATOR","ADMINISTRATOR"];
  if(!allowed.includes(role.toUpperCase())) return showToast("Invalid role");
  if(role.toUpperCase() === "ADMINISTRATOR" && !state.users.some(x => x.role === "ADMINISTRATOR")){
    u.role = "ADMINISTRATOR";
  } else if(role.toUpperCase() === "ADMINISTRATOR"){
    showToast("Only one administrator is allowed");
    return;
  } else {
    u.role = role.toUpperCase();
  }
  renderUsers();
  showToast("User role updated");
}

document.getElementById("profileForm").addEventListener("submit", e => {
  e.preventDefault();
  const name = document.getElementById("profileName").value;
  document.getElementById("miniName").textContent = name;
  document.getElementById("miniRole").textContent = document.getElementById("profileRole").value;
  showToast("Profile updated successfully");
});

function resetProfile(){
  document.getElementById("profileName").value="Ravi Kumar";
  document.getElementById("profileEmail").value="ravi.kiran@example.com";
  document.getElementById("profilePhone").value="9876543210";
  document.getElementById("profileRole").value="BUSINESS_CLIENT";
}

document.getElementById("businessForm").addEventListener("submit", e => {
  e.preventDefault();
  showToast("Business account details saved (demo)");
});

document.getElementById("shipmentForm").addEventListener("submit", e => {
  e.preventDefault();
  const id = "ST-" + new Date().getFullYear() + "-" + Math.floor(1000 + Math.random()*8999);
  const s = {
    id,
    routeId:null,
    destination: document.getElementById("deliveryAddress").value.split(",")[0],
    created: new Date().toLocaleDateString("en-GB",{day:"2-digit",month:"short",year:"numeric"}),
    status:"In Transit",
    receiver:document.getElementById("receiverName").value,
    phone:document.getElementById("receiverPhone").value,
    address:document.getElementById("deliveryAddress").value,
    eta:document.getElementById("estimatedDelivery").value,
    timeline:[["Shipment Created",new Date().toLocaleString(),"Shipment has been registered."]]
  };
  state.shipments.unshift(s);
  e.target.reset();
  showToast(`Shipment ${id} created successfully. Assign a route before live tracking.`);
  navigate("shipments");
});

// =========================================================
// CLEANUP
// =========================================================

window.addEventListener("beforeunload", () => {
  disconnectTracking();
});

function showToast(message){
  const t=document.getElementById("toast");
  t.textContent=message;t.classList.add("show");
  clearTimeout(window.toastTimer);
  window.toastTimer=setTimeout(()=>t.classList.remove("show"),2500);
}

function logout(){
  disconnectTracking();
  showToast("Logged out (frontend demo)");
}

renderRecent();
renderShipments();
renderUsers();
