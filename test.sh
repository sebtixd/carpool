java -cp "bin:lib/*" com.carpool.server.WebAppServer > server.log 2>&1 &
SERVER_PID=$!
sleep 2

echo "Trajets Endpoint:"
curl -s http://localhost:8080/api/trajets
echo -e "\n\nDashboard P1 before:"
curl -s "http://localhost:8080/api/dashboard?userId=P1"

echo -e "\n\nSubmitting Reservation..."
curl -s -X POST -d "trajetId=T2&userId=P1" "http://localhost:8080/api/reservations" || true

echo -e "\n\nDashboard P1 after:"
curl -s "http://localhost:8080/api/dashboard?userId=P1"

kill $SERVER_PID
