export CLIENT_ID=$(cat credentials.json | jq -r ".clientId")
export CLIENT_SECRET=$(cat credentials.json | jq -r ".client_secret")
echo $CLIENT_ID

export TOKEN=$(curl -X POST "https://auth.opensky-network.org/auth/realms/opensky-network/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=$CLIENT_ID" \
  -d "client_secret=$CLIENT_SECRET" | jq -r .access_token)

echo $TOKEN