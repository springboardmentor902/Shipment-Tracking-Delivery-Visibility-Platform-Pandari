/* =========================================================
   ShipTrack Pro Frontend Suite
   Complete Work: ETA/Risk Widget, Live Monitoring,
   Business Watchlist, Operator Signature & POD, Notifications,
   Customer/Business/Admin Analytics, Reports Export & Route History
   ========================================================= */

const CONFIG = {
  API_BASE_URL: "http://localhost:8081",
  WEBSOCKET_ENDPOINT: "/ws",
  LOCATION_TOPIC_PREFIX: "/topic/routes",
  TOKEN_STORAGE_KEY: "token",
  ROUTE_ENDPOINT: routeId => `/api/routes/${routeId}`,
  OSRM_URL: "https://router.project-osrm.org/route/v1/driving"
};

// Current Session Context
let currentAuthUser = {
  name: "Ravi Kumar",
  email: "ravi.kiran@example.com",
  role: "BUSINESS_CLIENT", // "BUSINESS_CLIENT" | "CUSTOMER" | "OPERATOR" | "ADMINISTRATOR"
  businessName: "Nexus Retailers Pvt Ltd",
  businessId: "BIZ-8802"
};

// Application State
const state = {
  shipments: [
    {
      id: "ST-2026-1042",
      routeId: 1,
      businessId: "BIZ-8802",
      destination: "Hyderabad, Telangana",
      created: "18 Aug 2026",
      status: "In Transit",
      receiver: "Anil Kumar",
      phone: "9876501234",
      address: "Madhapur, Hitech City, Hyderabad",
      eta: "21 Aug 2026, 04:30 PM",
      delayRiskScore: 68, // > 50 -> At Risk
      predictedDeliveryTime: "21 Aug 2026, 07:15 PM (+2.75 hrs delay)",
      delayFactor: "Heavy NH44 Highway Congestion & Rain",
      coordinates: { lat: 17.4483, lng: 78.3915 },
      routes: [
        { routeId: 1, origin: "Secunderabad Hub", destination: "Madhapur Hub, Hyderabad", distanceKm: 24.5, createdAt: "18 Aug 2026, 09:30 AM", isCurrent: true }
      ],
      pod: null,
      timeline: [
        ["Shipment Created", "18 Aug 2026, 09:20 AM", "Shipment registered by business client."],
        ["Picked Up", "18 Aug 2026, 12:10 PM", "Package picked up from fulfillment hub."],
        ["In Transit", "18 Aug 2026, 04:45 PM", "Package en route."]
      ]
    },
    {
      id: "ST-2026-1038",
      routeId: 2,
      businessId: "BIZ-8802",
      destination: "Vijayawada, Andhra Pradesh",
      created: "16 Aug 2026",
      status: "Out for Delivery",
      receiver: "Priya Reddy",
      phone: "9988776655",
      address: "Benz Circle, Vijayawada",
      eta: "18 Aug 2026, 01:00 PM",
      delayRiskScore: 22, // Low risk
      predictedDeliveryTime: "18 Aug 2026, 12:45 PM (On Time)",
      delayFactor: "Optimal weather and clear highway transit",
      coordinates: { lat: 16.5062, lng: 80.6480 },
      routes: [
        { routeId: 2, origin: "Guntur Sorting Facility", destination: "Benz Circle, Vijayawada", distanceKm: 38.2, createdAt: "16 Aug 2026, 10:15 AM", isCurrent: true }
      ],
      pod: null,
      timeline: [
        ["Shipment Created", "16 Aug 2026, 10:15 AM", "Shipment registered."],
        ["In Transit", "17 Aug 2026, 07:40 PM", "Reached Vijayawada hub."],
        ["Out for Delivery", "18 Aug 2026, 08:20 AM", "Courier assigned for delivery."]
      ]
    },
    {
      id: "ST-2026-1029",
      routeId: 3,
      businessId: "BIZ-8802",
      destination: "Warangal, Telangana",
      created: "12 Aug 2026",
      status: "Delivered",
      receiver: "Suresh Rao",
      phone: "9000012345",
      address: "Hanamkonda, Warangal",
      eta: "15 Aug 2026, 02:00 PM",
      delayRiskScore: 12,
      predictedDeliveryTime: "15 Aug 2026, 02:10 PM",
      delayFactor: "None",
      coordinates: { lat: 17.9689, lng: 79.5941 },
      routes: [
        { routeId: 3, origin: "Hyderabad Central", destination: "Hanamkonda, Warangal", distanceKm: 145.0, createdAt: "12 Aug 2026, 11:00 AM", isCurrent: true }
      ],
      pod: {
        recipientName: "Suresh Rao",
        notes: "Received in good condition at gate.",
        timestamp: "15 Aug 2026, 02:10 PM",
        photoUrl: "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?w=400&auto=format&fit=crop&q=60",
        signatureData: "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='200' height='60'><path d='M10 40 Q 50 10 90 35 T 180 20' stroke='%232563eb' stroke-width='3' fill='none'/></svg>"
      },
      timeline: [
        ["Shipment Created", "12 Aug 2026, 11:00 AM", "Shipment registered."],
        ["Delivered", "15 Aug 2026, 02:10 PM", "Package delivered and verified via POD."]
      ]
    },
    {
      id: "ST-2026-1077",
      routeId: 4,
      businessId: "BIZ-OTHER",
      destination: "Bengaluru, Karnataka",
      created: "19 Aug 2026",
      status: "In Transit",
      receiver: "Divya Nair",
      phone: "9123456780",
      address: "Indiranagar, Bengaluru",
      eta: "22 Aug 2026, 11:00 AM",
      delayRiskScore: 78, // At risk
      predictedDeliveryTime: "22 Aug 2026, 04:30 PM (+5.5 hrs delay)",
      delayFactor: "Interstate border checkpoint clearance delay",
      coordinates: { lat: 12.9716, lng: 77.5946 },
      routes: [
        { routeId: 4, origin: "Hosur Hub", destination: "Indiranagar, Bengaluru", distanceKm: 42.0, createdAt: "19 Aug 2026, 08:00 AM", isCurrent: true }
      ],
      pod: null,
      timeline: [
        ["Shipment Created", "19 Aug 2026, 08:00 AM", "Shipment created."]
      ]
    }
  ],
  notifications: [
    { id: 1, title: "Delay Alert: ST-2026-1042", message: "Risk score increased to 68% due to highway rainfall.", time: "10 mins ago", read: false, shipmentId: "ST-2026-1042" },
    { id: 2, title: "Out for Delivery", message: "Shipment ST-2026-1038 is assigned to carrier Arjun Rao.", time: "1 hour ago", read: false, shipmentId: "ST-2026-1038" },
    { id: 3, title: "Proof of Delivery Submitted", message: "Shipment ST-2026-1029 delivered to Suresh Rao.", time: "Yesterday", read: true, shipmentId: "ST-2026-1029" }
  ],
  users: [
    { name: "Ravi Kumar", email: "ravi.kiran@example.com", role: "BUSINESS_CLIENT", status: "ACTIVE" },
    { name: "Admin User", email: "admin@shiptrack.com", role: "ADMINISTRATOR", status: "ACTIVE" },
    { name: "Meena Sharma", email: "meena@example.com", role: "CUSTOMER", status: "ACTIVE" },
    { name: "Arjun Rao", email: "arjun@example.com", role: "OPERATOR", status: "ACTIVE" }
  ],
  routeAnalytics: {
    avgDistanceKm: 248.4,
    timeAccuracyPct: 94.2,
    reRouteCount: 3,
    bestCorridors: [
      { name: "Hyderabad ⇄ Warangal (NH163)", avgTime: "2h 45m", accuracy: "98.1%" },
      { name: "Guntur ⇄ Vijayawada (Expressway)", avgTime: "55m", accuracy: "96.4%" }
    ],
    worstCorridors: [
      { name: "Bengaluru ⇄ Hosur (NH44 Border)", avgTime: "3h 10m", accuracy: "64.8%", issue: "Severe Toll/Border Congestion" },
      { name: "Hyderabad ⇄ Kurnool (NH44 South)", avgTime: "4h 20m", accuracy: "72.0%", issue: "Monsoon Roadwork" }
    ]
  }
};

const sections = [
  "dashboard", "shipments", "details", "create-shipment",
  "live-monitoring", "business-dashboard", "operator-delivery",
  "analytics", "reports", "admin-routes", "admin", "profile", "settings"
];

// Active Telemetry References
let currentShipmentId = null;
let currentRouteId = null;
let trackingMap = null;
let liveMonMap = null;
let vehicleMarker = null;
let liveMonVehicleMarker = null;
let routeLine = null;
let stompClient = null;
let currentSubscription = null;

// =========================================================
// NAVIGATION & BOOTSTRAP
// =========================================================

document.querySelectorAll(".nav-item").forEach(btn => {
  btn.addEventListener("click", () => navigate(btn.dataset.section));
});

function navigate(section) {
  if (section !== "details" && section !== "live-monitoring") {
    disconnectTracking();
  }

  sections.forEach(s => document.getElementById(s)?.classList.remove("active"));
  const target = document.getElementById(section);
  if (target) target.classList.add("active");

  document.querySelectorAll(".nav-item").forEach(b => b.classList.toggle("active", b.dataset.section === section));

  const titles = {
    dashboard: "Operations Dashboard",
    shipments: "My Shipments",
    details: "Shipment Details & POD",
    "create-shipment": "Create Shipment",
    "live-monitoring": "Live Telemetry & Delay Radar",
    "business-dashboard": "Business Client Watchlist",
    "operator-delivery": "Operator Complete Delivery (POD)",
    analytics: "Analytics Dashboard Hub",
    reports: "Reports & Export Center",
    "admin-routes": "Admin Route Analytics",
    admin: "Manage Users",
    profile: "User Profile",
    settings: "Account Settings"
  };

  document.getElementById("pageTitle").textContent = titles[section] || "Dashboard";

  // Section initialization hooks
  if (section === "dashboard") renderDashboardStats();
  if (section === "shipments") renderShipments();
  if (section === "business-dashboard") renderBusinessDashboard();
  if (section === "operator-delivery") initOperatorDeliveryScreen();
  if (section === "live-monitoring") initLiveMonitoringDashboard();
  if (section === "analytics") renderAnalyticsDashboard("BUSINESS_CLIENT");
  if (section === "admin-routes") renderAdminRouteAnalytics();
  if (section === "admin") renderUsers();

  window.scrollTo({ top: 0, behavior: "smooth" });
}

// =========================================================
// NOTIFICATION MODULE
// =========================================================

function renderNotifications() {
  const notifListEl = document.getElementById("notifList");
  const countEl = document.getElementById("notifCount");

  const unreadCount = state.notifications.filter(n => !n.read).length;
  countEl.textContent = unreadCount;
  countEl.style.display = unreadCount > 0 ? "inline-block" : "none";

  // Display newest first
  const sorted = [...state.notifications].sort((a, b) => b.id - a.id);

  notifListEl.innerHTML = sorted.length ? sorted.map(n => `
    <div class="notif-item ${n.read ? "" : "unread"}" onclick="onNotificationClick(${n.id})">
      <strong>${n.title}</strong>
      <p>${n.message}</p>
      <time>${n.time}</time>
    </div>
  `).join("") : `<div style="padding:20px; text-align:center; color:var(--text-muted); font-size:12px;">No notifications</div>`;
}

function toggleNotifications() {
  const dropdown = document.getElementById("notificationDropdown");
  dropdown.classList.toggle("show");
}

function onNotificationClick(id) {
  const item = state.notifications.find(n => n.id === id);
  if (!item) return;

  item.read = true;
  renderNotifications();

  if (item.shipmentId) {
    document.getElementById("notificationDropdown").classList.remove("show");
    viewShipment(item.shipmentId);
  }
}

function markAllNotificationsAsRead() {
  state.notifications.forEach(n => (n.read = true));
  renderNotifications();
  showToast("All notifications marked as read");
}

// Close notification on outer click
document.addEventListener("click", e => {
  const notifWrapper = document.querySelector(".notification-wrapper");
  if (notifWrapper && !notifWrapper.contains(e.target)) {
    document.getElementById("notificationDropdown")?.classList.remove("show");
  }
});

// =========================================================
// DELAY RISK & ETA HELPER
// =========================================================

function getRiskCategory(score) {
  if (score >= 65) return { label: "HIGH RISK", class: "danger", badge: "badge-danger", fill: "high" };
  if (score >= 40) return { label: "MEDIUM RISK", class: "warning", badge: "badge-warning", fill: "medium" };
  return { label: "LOW RISK", class: "success", badge: "badge-success", fill: "low" };
}

function buildEtaRiskWidgetHtml(shipment) {
  const risk = getRiskCategory(shipment.delayRiskScore);
  return `
    <div class="card-head">
      <div>
        <h3>ETA & Delay-Risk Assessment</h3>
        <p>Predictive machine learning telemetry & dynamic traffic delay scoring</p>
      </div>
      <span class="badge ${risk.badge}">${risk.label}</span>
    </div>
    <div class="eta-widget-grid">
      <div>
        <span class="eyebrow">PREDICTED ARRIVAL</span>
        <div style="font-size:17px; font-weight:700; color:#1e293b; margin-top:2px;">
          ${shipment.predictedDeliveryTime}
        </div>
        <small style="color:var(--text-muted);">Scheduled ETA: ${shipment.eta}</small>
      </div>

      <div>
        <span class="eyebrow">DELAY RISK SCORE</span>
        <div style="display:flex; justify-content:space-between; align-items:baseline;">
          <strong style="font-size:20px;">${shipment.delayRiskScore}%</strong>
          <small style="color:var(--text-muted);">Threshold: 50%</small>
        </div>
        <div class="risk-meter">
          <div class="risk-fill ${risk.fill}" style="width: ${shipment.delayRiskScore}%"></div>
        </div>
      </div>

      <div style="grid-column: span 2;">
        <span class="eyebrow">RISK FACTOR ANALYSIS</span>
        <p style="margin:4px 0 0; font-size:12.5px; color:#334155;">
          ${shipment.delayFactor || "Nominal conditions along scheduled highway transit."}
        </p>
      </div>
    </div>
  `;
}

// =========================================================
// SHIPMENT DETAIL PAGE & ROUTE HISTORY COMPONENT
// =========================================================

function viewShipment(id) {
  const s = state.shipments.find(x => x.id === id);
  if (!s) return;

  disconnectTracking();
  currentShipmentId = s.id;
  currentRouteId = s.routeId;

  document.getElementById("detailSubtitle").textContent = `${s.id} • ${s.destination}`;

  // 1. Render ETA & Delay Risk Widget
  document.getElementById("etaRiskWidgetContainer").innerHTML = buildEtaRiskWidgetHtml(s);

  // 2. Render Core Shipment Info + POD View Section
  let podSectionHtml = "";
  if (s.pod) {
    podSectionHtml = `
      <div class="pod-proof-card">
        <h4><span style="color:var(--success)">✓</span> Verified Proof of Delivery (POD)</h4>
        <div class="pod-grid">
          <div>
            <div class="detail-item"><span>Signed By:</span><strong>${s.pod.recipientName}</strong></div>
            <div class="detail-item"><span>Timestamp:</span><strong>${s.pod.timestamp}</strong></div>
            <div class="detail-item"><span>Operator Notes:</span><strong>${s.pod.notes || "None"}</strong></div>
          </div>
          <div>
            <span style="font-size:11px; font-weight:700; color:var(--text-muted); display:block; margin-bottom:6px;">RECIPIENT SIGNATURE & PHOTO:</span>
            <div style="display:flex; gap:10px;">
              <div class="pod-image-box" title="Signature">
                <img src="${s.pod.signatureData}" alt="Signature Proof" />
              </div>
              ${s.pod.photoUrl ? `
                <div class="pod-image-box" title="Delivery Photo">
                  <img src="${s.pod.photoUrl}" alt="Delivery Photo" />
                </div>` : ""}
            </div>
          </div>
        </div>
      </div>
    `;
  } else {
    podSectionHtml = `
      <div style="padding:14px; background:#f8fafc; border:1px dashed #cbd5e1; border-radius:8px; margin-top:14px; font-size:12.5px; color:var(--text-muted);">
        ℹ Proof of Delivery has not been captured yet. Complete delivery via the Operator portal.
      </div>
    `;
  }

  document.getElementById("shipmentDetails").innerHTML = `
    <div class="details-grid">
      <div class="detail-card">
        <h4>Shipment Information</h4>
        <div class="detail-item"><span>Tracking Number</span><strong>${s.id}</strong></div>
        <div class="detail-item"><span>Status</span><strong>${statusBadge(s.status)}</strong></div>
        <div class="detail-item"><span>Consignee / Receiver</span><strong>${s.receiver}</strong></div>
        <div class="detail-item"><span>Contact Phone</span><strong>${s.phone}</strong></div>
        <div class="detail-item"><span>Delivery Destination</span><strong>${s.address}</strong></div>
        <div class="detail-item"><span>Assigned Route ID</span><strong>${s.routeId ?? "Not assigned"}</strong></div>
        ${podSectionHtml}
      </div>
      <div class="detail-card">
        <h4>Tracking Timeline</h4>
        <div class="timeline">
          ${s.timeline.map(t => `<div class="timeline-item"><span class="timeline-dot"></span><strong>${t[0]}</strong><p>${t[1]}</p><p>${t[2]}</p></div>`).join("")}
        </div>
      </div>
    </div>
  `;

  // 3. Render Route History (Origin, Destination, Distance, Date, Current vs Previous)
  renderRouteHistoryComponent(s);

  navigate("details");

  // 4. Initialize Map & Telemetry
  initDetailMap(s);
}

// RouteHistory.jsx native component simulator
function renderRouteHistoryComponent(shipment) {
  const container = document.getElementById("routeHistoryContainer");
  if (!container) return;

  const routes = shipment.routes || [];
  if (!routes.length) {
    container.innerHTML = `<p style="font-size:13px; color:var(--text-muted); padding:10px 0;">No route history recorded.</p>`;
    return;
  }

  // Sorted latest first
  const sorted = [...routes].sort((a, b) => (b.routeId || 0) - (a.routeId || 0));

  container.innerHTML = `
    <div class="route-history-list">
      ${sorted.map(r => `
        <div class="route-card ${r.isCurrent ? "current" : ""}">
          <div>
            <div style="display:flex; align-items:center; gap:8px;">
              <strong style="font-size:13.5px;">Route #${r.routeId}: ${r.origin} ➔ ${r.destination}</strong>
              <span class="badge ${r.isCurrent ? "badge-success" : "badge-secondary"}">
                ${r.isCurrent ? "CURRENT ACTIVE ROUTE" : "PREVIOUS ROUTE"}
              </span>
            </div>
            <div class="route-meta">
              <span>📍 Distance: <strong>${r.distanceKm} km</strong></span>
              <span>🕒 Created: <strong>${r.createdAt}</strong></span>
            </div>
          </div>
          <div>
            ${r.isCurrent ? `<span style="font-size:12px; color:var(--primary); font-weight:600;">Active Corridor</span>` : `<span style="font-size:12px; color:var(--text-muted);">Re-routed / Archived</span>`}
          </div>
        </div>
      `).join("")}
    </div>
  `;
}

// Simulate Re-Route Deliverable
function simulateReRouteForCurrent() {
  const shipment = state.shipments.find(s => s.id === currentShipmentId);
  if (!shipment) return;

  const newRouteId = shipment.routes.length + 10;
  const newDistance = (shipment.routes[0].distanceKm + (Math.random() * 8 + 3)).toFixed(1);

  // Mark previous as non-current
  shipment.routes.forEach(r => (r.isCurrent = false));

  // Push new current route
  shipment.routes.unshift({
    routeId: newRouteId,
    origin: "Secunderabad Bypass Diversion Hub",
    destination: shipment.destination,
    distanceKm: parseFloat(newDistance),
    createdAt: new Date().toLocaleString(),
    isCurrent: true
  });

  shipment.routeId = newRouteId;
  shipment.delayRiskScore = Math.max(15, shipment.delayRiskScore - 25);
  shipment.predictedDeliveryTime = "Re-calculated: +35 mins delay (Traffic avoided)";
  shipment.timeline.unshift([
    "Dynamic Re-Route Applied",
    new Date().toLocaleString(),
    `Re-routed via Route #${newRouteId} to avoid highway obstruction.`
  ]);

  // Update Admin Route Analytics telemetry
  state.routeAnalytics.reRouteCount += 1;

  // Add Notification
  state.notifications.unshift({
    id: Date.now(),
    title: `Dynamic Re-Route: ${shipment.id}`,
    message: `Active path shifted to Route #${newRouteId} (${newDistance} km).`,
    time: "Just now",
    read: false,
    shipmentId: shipment.id
  });

  renderNotifications();
  showToast(`Simulated re-route successfully. Route #${newRouteId} is now CURRENT.`);
  viewShipment(shipment.id);
}

// Map Initialization
function initDetailMap(shipment) {
  const lat = shipment.coordinates?.lat || 17.3850;
  const lng = shipment.coordinates?.lng || 78.4867;

  if (trackingMap) {
    trackingMap.remove();
    trackingMap = null;
  }

  trackingMap = L.map("trackingMap").setView([lat, lng], 11);
  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    attribution: "&copy; OpenStreetMap contributors"
  }).addTo(trackingMap);

  vehicleMarker = L.marker([lat, lng]).addTo(trackingMap).bindPopup(`<b>${shipment.id}</b><br>Live Telemetry Location`);
  routeLine = L.polyline([[lat, lng], [lat + 0.05, lng + 0.04]], { weight: 5, color: "#2563eb" }).addTo(trackingMap);

  document.getElementById("routeInfo").textContent = `Route ID: ${shipment.routeId || "—"}`;
  document.getElementById("lastLocation").textContent = `Tracking GPS: ${lat.toFixed(4)}, ${lng.toFixed(4)}`;

  // Connect STOMP if routeId exists
  if (shipment.routeId) {
    connectWebSocket(shipment.routeId);
  }
}

// =========================================================
// LIVE DELIVERY MONITORING DASHBOARD
// =========================================================

function initLiveMonitoringDashboard() {
  const tableBody = document.getElementById("liveMonitoringSelectionTable");
  const inTransit = state.shipments.filter(s => s.status !== "Delivered" && s.status !== "Cancelled");

  tableBody.innerHTML = inTransit.map(s => {
    const risk = getRiskCategory(s.delayRiskScore);
    return `
      <tr>
        <td><strong>${s.id}</strong></td>
        <td>${s.destination}</td>
        <td><span class="badge ${risk.badge}">${s.delayRiskScore}%</span></td>
        <td><button class="primary btn-sm" onclick="selectForLiveMonitoring('${s.id}')">Monitor</button></td>
      </tr>
    `;
  }).join("");

  if (inTransit.length > 0) {
    selectForLiveMonitoring(inTransit[0].id);
  }
}

function selectForLiveMonitoring(id) {
  const shipment = state.shipments.find(s => s.id === id);
  if (!shipment) return;

  const widgetEl = document.getElementById("liveMonitoringWidget");
  widgetEl.innerHTML = buildEtaRiskWidgetHtml(shipment);

  const statusBox = document.getElementById("liveMonTrackingStatus");
  statusBox.className = "tracking-status connected";
  statusBox.querySelector("strong").textContent = `Live Telemetry: ${shipment.id}`;

  const lat = shipment.coordinates?.lat || 17.3850;
  const lng = shipment.coordinates?.lng || 78.4867;

  if (liveMonMap) {
    liveMonMap.remove();
    liveMonMap = null;
  }

  liveMonMap = L.map("liveMonMap").setView([lat, lng], 12);
  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    attribution: "&copy; OpenStreetMap contributors"
  }).addTo(liveMonMap);

  liveMonVehicleMarker = L.marker([lat, lng]).addTo(liveMonMap).bindPopup(`Live GPS: ${shipment.id}`).openPopup();
  L.polyline([[lat, lng], [lat + 0.03, lng + 0.03]], { weight: 5, color: "#2563eb" }).addTo(liveMonMap);
}

// =========================================================
// BUSINESS CLIENT DASHBOARD & AT-RISK LIST
// =========================================================

function renderBusinessDashboard() {
  // Scoped to business user's data
  const bizShipments = state.shipments.filter(s => s.businessId === currentAuthUser.businessId);

  const threshold = 50;
  const atRiskList = bizShipments.filter(s => s.delayRiskScore > threshold && s.status !== "Delivered");

  document.getElementById("bizTotalActive").textContent = bizShipments.filter(s => s.status !== "Delivered").length;
  document.getElementById("bizCriticalCount").textContent = atRiskList.length;
  document.getElementById("bizDelayedCost").textContent = `₹${(atRiskList.length * 4500).toLocaleString()}`;
  document.getElementById("atRiskBadge").textContent = `${atRiskList.length} Critical Shipments`;

  const tbody = document.getElementById("atRiskTableBody");
  if (!atRiskList.length) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding:20px; color:var(--text-muted);">No shipments currently exceed the 50% delay-risk threshold.</td></tr>`;
    return;
  }

  tbody.innerHTML = atRiskList.map(s => `
    <tr>
      <td><strong>${s.id}</strong></td>
      <td>${s.destination}</td>
      <td>${s.eta}</td>
      <td>
        <span class="badge badge-danger">${s.delayRiskScore}% Delay Risk</span>
      </td>
      <td style="color:#b91c1c; font-weight:500;">${s.delayFactor || "Highway Congestion"}</td>
      <td>
        <button class="primary btn-sm" onclick="viewShipment('${s.id}')">Inspect Telemetry</button>
      </td>
    </tr>
  `).join("");
}

// =========================================================
// OPERATOR COMPLETE DELIVERY (SIGNATURE & PHOTO POD)
// =========================================================

let signaturePadCanvas = null;
let signaturePadCtx = null;
let isDrawingSignature = false;
let uploadedPhotoBase64 = null;

function initOperatorDeliveryScreen() {
  const select = document.getElementById("podShipmentSelect");
  const available = state.shipments.filter(s => s.status !== "Delivered" && s.status !== "Cancelled");

  select.innerHTML = `<option value="">-- Choose Shipment to Finalize --</option>` +
    available.map(s => `<option value="${s.id}">${s.id} — ${s.receiver} (${s.destination})</option>`).join("");

  setupSignatureCanvas();
}

function onSelectPodShipment(shipmentId) {
  const s = state.shipments.find(x => x.id === shipmentId);
  if (s) {
    document.getElementById("podRecipientName").value = s.receiver;
  }
}

function setupSignatureCanvas() {
  signaturePadCanvas = document.getElementById("signatureCanvas");
  if (!signaturePadCanvas) return;
  signaturePadCtx = signaturePadCanvas.getContext("2d");

  // Reset resolution
  signaturePadCanvas.width = signaturePadCanvas.offsetWidth || 600;
  signaturePadCanvas.height = 160;
  signaturePadCtx.strokeStyle = "#1d4ed8";
  signaturePadCtx.lineWidth = 2.5;
  signaturePadCtx.lineCap = "round";

  const getPos = e => {
    const rect = signaturePadCanvas.getBoundingClientRect();
    const clientX = e.touches ? e.touches[0].clientX : e.clientX;
    const clientY = e.touches ? e.touches[0].clientY : e.clientY;
    return { x: clientX - rect.left, y: clientY - rect.top };
  };

  const startDraw = e => {
    isDrawingSignature = true;
    const pos = getPos(e);
    signaturePadCtx.beginPath();
    signaturePadCtx.moveTo(pos.x, pos.y);
  };

  const draw = e => {
    if (!isDrawingSignature) return;
    e.preventDefault();
    const pos = getPos(e);
    signaturePadCtx.lineTo(pos.x, pos.y);
    signaturePadCtx.stroke();
  };

  const stopDraw = () => (isDrawingSignature = false);

  signaturePadCanvas.onmousedown = startDraw;
  signaturePadCanvas.onmousemove = draw;
  signaturePadCanvas.onmouseup = stopDraw;

  signaturePadCanvas.ontouchstart = startDraw;
  signaturePadCanvas.ontouchmove = draw;
  signaturePadCanvas.ontouchend = stopDraw;
}

function clearSignature() {
  if (!signaturePadCtx || !signaturePadCanvas) return;
  signaturePadCtx.clearRect(0, 0, signaturePadCanvas.width, signaturePadCanvas.height);
}

function previewPodPhoto(event) {
  const file = event.target.files[0];
  if (!file) return;

  const reader = new FileReader();
  reader.onload = e => {
    uploadedPhotoBase64 = e.target.result;
    const preview = document.getElementById("podPhotoPreview");
    preview.src = uploadedPhotoBase64;
    preview.classList.remove("hidden");
    document.getElementById("podPhotoPrompt").style.display = "none";
  };
  reader.readAsDataURL(file);
}

// Submit Proof of Delivery Form Handler
document.getElementById("completeDeliveryForm")?.addEventListener("submit", e => {
  e.preventDefault();

  const shipmentId = document.getElementById("podShipmentSelect").value;
  const recipientName = document.getElementById("podRecipientName").value;
  const notes = document.getElementById("podDeliveryNotes").value;

  if (!shipmentId) return showToast("Select an active shipment first.");

  const signatureDataUrl = signaturePadCanvas.toDataURL("image/png");

  // End-to-end status and POD record update
  const shipment = state.shipments.find(s => s.id === shipmentId);
  if (!shipment) return;

  shipment.status = "Delivered";
  shipment.delayRiskScore = 0;
  shipment.predictedDeliveryTime = `Delivered on ${new Date().toLocaleTimeString()}`;

  shipment.pod = {
    recipientName,
    notes,
    timestamp: new Date().toLocaleString(),
    signatureData: signatureDataUrl,
    photoUrl: uploadedPhotoBase64 || "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?w=400&auto=format&fit=crop&q=60"
  };

  shipment.timeline.push([
    "Delivered & Verified",
    new Date().toLocaleString(),
    `Package accepted by ${recipientName}. Signature & photo proof recorded.`
  ]);

  // Create real-time notification
  state.notifications.unshift({
    id: Date.now(),
    title: `Delivery Completed: ${shipment.id}`,
    message: `Delivered to ${recipientName}. Proof of delivery verified.`,
    time: "Just now",
    read: false,
    shipmentId: shipment.id
  });

  renderNotifications();
  showToast(`Shipment ${shipment.id} successfully marked as Delivered.`);

  // Reset form
  e.target.reset();
  clearSignature();
  uploadedPhotoBase64 = null;
  document.getElementById("podPhotoPreview").classList.add("hidden");
  document.getElementById("podPhotoPrompt").style.display = "block";

  // Redirect directly to details to verify view
  viewShipment(shipment.id);
});

// =========================================================
// ANALYTICS DASHBOARDS (CUSTOMER, BUSINESS & ADMIN)
// =========================================================

function switchAnalyticsRole(role, el) {
  document.querySelectorAll(".role-pill").forEach(p => p.classList.remove("active"));
  if (el) el.classList.add("active");
  renderAnalyticsDashboard(role);
}

function renderAnalyticsDashboard(role) {
  const container = document.getElementById("analyticsRoleContent");
  if (!container) return;

  if (role === "CUSTOMER") {
    // Customer Scoped Analytics
    container.innerHTML = `
      <div class="stats">
        <div class="stat-card"><span>My Total Packages</span><strong>12</strong><small>Lifetime orders</small></div>
        <div class="stat-card"><span>On-Time Arrival</span><strong style="color:var(--success)">100%</strong><small>No missed deliveries</small></div>
        <div class="stat-card"><span>Active Deliveries</span><strong>1</strong><small>Arriving today</small></div>
        <div class="stat-card"><span>Saved Delivery Locations</span><strong>3</strong><small>Primary: Madhapur</small></div>
      </div>
      <div class="card">
        <div class="card-head"><div><h3>My Delivery Carbon & Transit History</h3><p>Personal logistics consumption</p></div></div>
        <div style="padding:15px; background:#f8fafc; border-radius:8px; font-size:13px;">
          ✓ Average transit time: <strong>1.4 days</strong><br>
          ✓ 100% of deliveries verified with digital Proof of Delivery (POD).
        </div>
      </div>
    `;
  } else if (role === "BUSINESS_CLIENT") {
    // Strictly Business Scoped Analytics
    const bizShipments = state.shipments.filter(s => s.businessId === currentAuthUser.businessId);
    container.innerHTML = `
      <div class="stats">
        <div class="stat-card"><span>Company Active Consignments</span><strong>${bizShipments.length}</strong><small>${currentAuthUser.businessName}</small></div>
        <div class="stat-card"><span>SLA Adherence</span><strong style="color:var(--success)">96.4%</strong><small>Target: 95%</small></div>
        <div class="stat-card"><span>Avg Transit Duration</span><strong>31.2 hrs</strong><small>Regional South Corridor</small></div>
        <div class="stat-card"><span>POD Fulfillment Rate</span><strong style="color:var(--primary)">100%</strong><small>Zero missing signatures</small></div>
      </div>
      <div class="grid-2">
        <div class="card">
          <div class="card-head"><div><h3>Business Cost Per Delivery</h3><p>Optimized freight expenditure</p></div></div>
          <table style="margin-top:10px;">
            <tr><th>Corridor</th><th>Volume</th><th>Avg Cost/Shipment</th></tr>
            <tr><td>Hyderabad ⇄ Vijayawada</td><td>420 units</td><td>₹184</td></tr>
            <tr><td>Hyderabad ⇄ Warangal</td><td>210 units</td><td>₹142</td></tr>
          </table>
        </div>
        <div class="card">
          <div class="card-head"><div><h3>Delay Causes Breakdown</h3><p>Top triggers for risk score elevations</p></div></div>
          <div style="padding-top:8px; font-size:12.5px;">
            <div style="display:flex; justify-content:space-between; margin-bottom:6px;"><span>Interstate Monsoon Rains</span><strong>48%</strong></div>
            <div style="display:flex; justify-content:space-between; margin-bottom:6px;"><span>Border Checkpoint Queues</span><strong>32%</strong></div>
            <div style="display:flex; justify-content:space-between;"><span>Highway Lane Closures</span><strong>20%</strong></div>
          </div>
        </div>
      </div>
    `;
  } else if (role === "ADMINISTRATOR") {
    // System Infrastructure & Admin Monitoring
    container.innerHTML = `
      <div class="stats">
        <div class="stat-card"><span>Fleet System Health</span><strong style="color:var(--success)">99.98%</strong><small>STOMP WS Uptime</small></div>
        <div class="stat-card"><span>Active Field Couriers</span><strong>42</strong><small>Online on mobile</small></div>
        <div class="stat-card"><span>Platform Load</span><strong>184 req/s</strong><small>API Gateway healthy</small></div>
        <div class="stat-card"><span>Registered Businesses</span><strong>128</strong><small>Commercial accounts</small></div>
      </div>
      <div class="card">
        <div class="card-head">
          <div><h3>System Telemetry & Reports Management</h3><p>Infrastructure controls and cluster nodes</p></div>
          <button class="primary btn-sm" onclick="showToast('Cluster cache flushed')">Purge Redis Cache</button>
        </div>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Microservice</th><th>Port / Path</th><th>Status</th><th>Latency</th></tr></thead>
            <tbody>
              <tr><td>Route & Telemetry Engine</td><td>:8081/ws</td><td><span class="status done">HEALTHY</span></td><td>14ms</td></tr>
              <tr><td>Delay Prediction (ML Model)</td><td>:8082/predict</td><td><span class="status done">HEALTHY</span></td><td>42ms</td></tr>
              <tr><td>POD S3 / Storage Service</td><td>:8083/pod</td><td><span class="status done">HEALTHY</span></td><td>28ms</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    `;
  }
}

// =========================================================
// REPORTS & EXPORT GENERATOR
// =========================================================

function generateAndDownloadReport() {
  const type = document.getElementById("reportTypeSelect").value;
  const format = document.getElementById("reportFormatSelect").value;
  const range = document.getElementById("reportDateRange").value;

  // Filter and restrict data according to user role
  let reportData = [];
  if (currentAuthUser.role === "BUSINESS_CLIENT") {
    reportData = state.shipments.filter(s => s.businessId === currentAuthUser.businessId);
  } else if (currentAuthUser.role === "ADMINISTRATOR") {
    reportData = state.shipments;
  } else {
    reportData = state.shipments.slice(0, 1);
  }

  const reportMetadata = {
    reportType: type,
    format,
    dateRangeDays: range,
    generatedForUser: currentAuthUser.email,
    userRole: currentAuthUser.role,
    generatedAt: new Date().toISOString(),
    recordCount: reportData.length,
    records: reportData
  };

  let fileBlob = null;
  let filename = `ShipTrack_${type}_${Date.now()}`;

  if (format === "PDF") {
    // Generate text/pdf representative export
    const pdfContent = `
=====================================================
            SHIPTRACK PRO ENTERPRISE REPORT
=====================================================
Report Type:   ${type}
User Scope:    ${currentAuthUser.role} (${currentAuthUser.name})
Business:      ${currentAuthUser.businessName}
Generated On:  ${new Date().toLocaleString()}
Time Window:   Last ${range} Days
Records Found: ${reportData.length}
=====================================================

` + reportData.map((r, i) => `
#${i + 1} TRACKING NO: ${r.id}
Destination:       ${r.destination}
Status:            ${r.status}
Delay Risk Score:  ${r.delayRiskScore}%
Predicted ETA:     ${r.predictedDeliveryTime}
POD Verified:      ${r.pod ? "YES (Signed by " + r.pod.recipientName + ")" : "NO"}
-----------------------------------------------------`).join("\n");

    fileBlob = new Blob([pdfContent], { type: "application/pdf" });
    filename += ".pdf";
  } else {
    // Excel / CSV Export
    const csvHeader = "TrackingID,Destination,Status,ETA,DelayRiskScore,POD_Verified\n";
    const csvRows = reportData.map(r =>
      `"${r.id}","${r.destination}","${r.status}","${r.eta}","${r.delayRiskScore}%","${r.pod ? "Yes" : "No"}"`
    ).join("\n");

    fileBlob = new Blob([csvHeader + csvRows], { type: "text/csv;charset=utf-8;" });
    filename += ".csv";
  }

  // Trigger download in browser
  const link = document.createElement("a");
  link.href = URL.createObjectURL(fileBlob);
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);

  showToast(`Report downloaded successfully (${filename})`);
}

// =========================================================
// ADMIN ROUTE ANALYTICS
// =========================================================

function renderAdminRouteAnalytics() {
  const ra = state.routeAnalytics;
  document.getElementById("adminAvgDistance").textContent = `${ra.avgDistanceKm} km`;
  document.getElementById("adminEstimateAccuracy").textContent = `${ra.timeAccuracyPct}%`;
  document.getElementById("adminReRouteCount").textContent = ra.reRouteCount;

  document.getElementById("bestCorridors").innerHTML = ra.bestCorridors.map(c => `
    <div class="corridor-item">
      <div><strong>${c.name}</strong><br><small style="color:var(--text-muted);">Avg Duration: ${c.avgTime}</small></div>
      <span class="badge badge-success">${c.accuracy} Accuracy</span>
    </div>
  `).join("");

  document.getElementById("worstCorridors").innerHTML = ra.worstCorridors.map(c => `
    <div class="corridor-item">
      <div><strong>${c.name}</strong><br><small style="color:var(--danger);">${c.issue}</small></div>
      <span class="badge badge-danger">${c.accuracy} Accuracy</span>
    </div>
  `).join("");
}

// =========================================================
// SHIPMENT LIST & RECENT
// =========================================================

function statusClass(status) {
  if (status === "Delivered") return "done";
  if (status === "Out for Delivery") return "out";
  if (status === "Cancelled") return "cancel";
  return "in";
}

function statusBadge(status) {
  return `<span class="status ${statusClass(status)}">${status}</span>`;
}

function renderDashboardStats() {
  const total = state.shipments.length;
  const inTransit = state.shipments.filter(s => s.status === "In Transit" || s.status === "Out for Delivery").length;
  const atRisk = state.shipments.filter(s => s.delayRiskScore > 50 && s.status !== "Delivered").length;
  const delivered = state.shipments.filter(s => s.status === "Delivered").length;

  document.getElementById("dashTotalShipments").textContent = total;
  document.getElementById("dashInTransit").textContent = inTransit;
  document.getElementById("dashAtRisk").textContent = atRisk;
  document.getElementById("dashDelivered").textContent = delivered;
  renderRecent();
}

function renderRecent() {
  const el = document.getElementById("recentShipments");
  el.innerHTML = state.shipments.slice(0, 4).map(s => {
    const risk = getRiskCategory(s.delayRiskScore);
    return `
      <div class="shipment-row" style="cursor:pointer;" onclick="viewShipment('${s.id}')">
        <div>
          <div class="tracking">${s.id}</div>
          <div class="destination">${s.destination} • ETA: ${s.eta}</div>
        </div>
        <div style="display:flex; gap:8px; align-items:center;">
          <span class="badge ${risk.badge}">${risk.label}</span>
          ${statusBadge(s.status)}
        </div>
      </div>
    `;
  }).join("");
}

function renderShipments() {
  const search = document.getElementById("shipmentSearch")?.value.toLowerCase() || "";
  const filter = document.getElementById("shipmentFilter")?.value || "";

  const rows = state.shipments.filter(s =>
    (s.id.toLowerCase().includes(search) || s.destination.toLowerCase().includes(search)) &&
    (!filter || s.status === filter)
  );

  document.getElementById("shipmentsTable").innerHTML = rows.length ? rows.map(s => {
    const risk = getRiskCategory(s.delayRiskScore);
    return `
      <tr>
        <td><strong>${s.id}</strong></td>
        <td>${s.destination}</td>
        <td>${s.eta}</td>
        <td><span class="badge ${risk.badge}">${s.delayRiskScore}%</span></td>
        <td>${statusBadge(s.status)}</td>
        <td>
          <button class="primary btn-sm" onclick="viewShipment('${s.id}')">View Details</button>
        </td>
      </tr>
    `;
  }).join("") : `<tr><td colspan="6">No matching shipments found.</td></tr>`;
}

// =========================================================
// WEBSOCKET STOMP INTEGRATION
// =========================================================

function connectWebSocket(routeId) {
  if (!routeId) return;
  disconnectWebSocketOnly();

  if (typeof SockJS === "undefined" || typeof StompJs === "undefined") return;

  const socket = new SockJS(`${CONFIG.API_BASE_URL}${CONFIG.WEBSOCKET_ENDPOINT}`);
  stompClient = new StompJs.Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,
    connectHeaders: { Authorization: `Bearer ${localStorage.getItem(CONFIG.TOKEN_STORAGE_KEY) || ""}` }
  });

  stompClient.onConnect = () => {
    stompClient.subscribe(`${CONFIG.LOCATION_TOPIC_PREFIX}/${routeId}`, message => {
      try {
        const location = JSON.parse(message.body);
        if (vehicleMarker && location.latitude && location.longitude) {
          vehicleMarker.setLatLng([location.latitude, location.longitude]);
          trackingMap?.panTo([location.latitude, location.longitude]);
        }
      } catch (err) {
        console.error("STOMP parse error:", err);
      }
    });
  };

  stompClient.activate();
}

function disconnectWebSocketOnly() {
  if (stompClient) {
    try { stompClient.deactivate(); } catch (e) {}
    stompClient = null;
  }
}

function disconnectTracking() {
  disconnectWebSocketOnly();
  if (trackingMap) { trackingMap.remove(); trackingMap = null; }
  if (liveMonMap) { liveMonMap.remove(); liveMonMap = null; }
}

// =========================================================
// UTILITIES & AUTH PROFILE
// =========================================================

function renderUsers() {
  const search = document.getElementById("userSearch")?.value.toLowerCase() || "";
  const filter = document.getElementById("roleFilter")?.value || "";

  const rows = state.users.filter(u =>
    (u.name + u.email).toLowerCase().includes(search) && (!filter || u.role === filter)
  );

  document.getElementById("usersTable").innerHTML = rows.map((u, i) => `
    <tr>
      <td><strong>${u.name}</strong></td>
      <td>${u.email}</td>
      <td>${u.role}</td>
      <td><span class="badge badge-success">${u.status}</span></td>
      <td><button class="action-link" onclick="changeRole(${i})">Edit Role</button></td>
    </tr>
  `).join("");
}

function changeRole(index) {
  const u = state.users[index];
  const role = prompt(`Enter role for ${u.name} (CUSTOMER, BUSINESS_CLIENT, OPERATOR, ADMINISTRATOR):`, u.role);
  if (role) {
    u.role = role.toUpperCase();
    renderUsers();
    showToast("Role updated");
  }
}

function showToast(message) {
  const t = document.getElementById("toast");
  t.textContent = message;
  t.classList.add("show");
  clearTimeout(window.toastTimer);
  window.toastTimer = setTimeout(() => t.classList.remove("show"), 2600);
}

function logout() {
  disconnectTracking();
  showToast("Session ended (Demo)");
}

// Initial Boot
renderDashboardStats();
renderShipments();
renderNotifications();