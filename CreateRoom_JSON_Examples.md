# QuizMaster - Exemplos de JSON Corrigidos

## Baseado nos erros encontrados e análise dos DTOs

### ❌ Erros Identificados:

#### Erro 1: Campo room_name null
```
"message": "could not execute statement [Column 'room_name' cannot be null]"
```

#### Erro 2: NullPointerException na lista de players
```
"message": "Cannot invoke \"java.util.List.stream()\" because the return value of \"quizmaster.quiz.models.Room.getPlayers()\" is null"
"path": "/api/rooms"
RoomService.convertToRoomResponse(RoomService.java:251)
```

### ✅ Soluções Aplicadas:

1. **Corrigir mapeamento de campos** - Usar `roomName` em vez de `name`
2. **Inicializar lista de players** - No RoomService.createRoom()
3. **Proteção null-safe** - No método convertToRoomResponse()

### ✅ Formato Correto para CreateRoomRequest:

```json
{
  "roomName": "Quiz em Equipe - Teste",
  "password": null,
  "gameMode": "TEAM",
  "difficulty": "MEDIUM", 
  "maxPlayers": 6,
  "questionTime": 25,
  "questionCount": 8,
  "categories": ["MATH", "SCIENCE", "GEOGRAPHY"],
  "assignmentType": "CHOOSE",
  "allowSpectators": false,
  "enableChat": true,
  "showRealTimeRanking": true,
  "allowReconnection": true
}
```

## Campos Obrigatórios vs Opcionais:

### ✅ Campos Obrigatórios:
- `roomName` (String) - Nome da sala
- `gameMode` (Enum) - TEAM, DUEL, CLASSIC, KAHOOT
- `difficulty` (Enum) - EASY, MEDIUM, HARD
- `maxPlayers` (Integer) - Máximo de jogadores
- `questionTime` (Integer) - Tempo por pergunta em segundos
- `questionCount` (Integer) - Número de perguntas
- `categories` (List<Category>) - Lista de categorias

### 🔧 Campos Opcionais:
- `password` (String) - Senha da sala (null para pública)
- `assignmentType` (Enum) - CHOOSE, RANDOM (padrão: CHOOSE)
- `allowSpectators` (Boolean) - Permitir espectadores (padrão: false)
- `enableChat` (Boolean) - Habilitar chat (padrão: true)
- `showRealTimeRanking` - Mostrar ranking em tempo real (padrão: true)
- `allowReconnection` (Boolean) - Permitir reconexão (padrão: true)

## Enums Válidos:

### GameMode:
- `TEAM` - Jogo em equipe
- `DUEL` - Duelo 1v1
- `CLASSIC` - Clássico individual
- `KAHOOT` - Estilo Kahoot

### Difficulty:
- `EASY` - Fácil
- `MEDIUM` - Médio
- `HARD` - Difícil

### Category:
- `MATH` - Matemática
- `PORTUGUESE` - Português
- `HISTORY` - História
- `GEOGRAPHY` - Geografia
- `SCIENCE` - Ciências
- `ENGLISH` - Inglês
- `MIXED` - Misto

### AssignmentType:
- `CHOOSE` - Jogadores escolhem equipe
- `RANDOM` - Atribuição aleatória

## Exemplos de Diferentes Tipos de Sala:

### 🔵 Sala Pública Individual:
```json
{
  "roomName": "Quiz Individual Público",
  "password": null,
  "gameMode": "CLASSIC",
  "difficulty": "EASY",
  "maxPlayers": 10,
  "questionTime": 30,
  "questionCount": 10,
  "categories": ["MATH", "SCIENCE"],
  "assignmentType": "CHOOSE",
  "allowSpectators": true,
  "enableChat": true,
  "showRealTimeRanking": true,
  "allowReconnection": true
}
```

### 🔒 Sala Privada com Senha:
```json
{
  "roomName": "Quiz Privado Família",
  "password": "familia123",
  "gameMode": "TEAM", 
  "difficulty": "MEDIUM",
  "maxPlayers": 6,
  "questionTime": 25,
  "questionCount": 15,
  "categories": ["GEOGRAPHY", "HISTORY"],
  "assignmentType": "RANDOM",
  "allowSpectators": false,
  "enableChat": true,
  "showRealTimeRanking": false,
  "allowReconnection": true
}
```

### ⚡ Sala Estilo Kahoot:
```json
{
  "roomName": "Quiz Kahoot Style",
  "password": null,
  "gameMode": "KAHOOT",
  "difficulty": "HARD",
  "maxPlayers": 20,
  "questionTime": 15,
  "questionCount": 20,
  "categories": ["MIXED"],
  "assignmentType": "CHOOSE",
  "allowSpectators": true,
  "enableChat": false,
  "showRealTimeRanking": true,
  "allowReconnection": false
}
```

### 🥊 Sala Duelo:
```json
{
  "roomName": "Duelo Matemática",
  "password": null,
  "gameMode": "DUEL",
  "difficulty": "HARD",
  "maxPlayers": 2,
  "questionTime": 20,
  "questionCount": 10,
  "categories": ["MATH"],
  "assignmentType": "CHOOSE",
  "allowSpectators": true,
  "enableChat": false,
  "showRealTimeRanking": true,
  "allowReconnection": true
}
```

## Dicas para Teste:

1. **Sempre use `roomName`** em vez de `name`
2. **Categorias devem ser um array** mesmo com uma categoria
3. **Enums são case-sensitive** - use MAIÚSCULAS
4. **Campos boolean** devem ser true/false, não strings
5. **Password null** para salas públicas
6. **maxPlayers** deve ser compatível com gameMode (DUEL = máx 2)

## Validações Importantes:

- ✅ roomName não pode ser vazio/null
- ✅ maxPlayers > 0
- ✅ questionTime > 0
- ✅ questionCount > 0
- ✅ categories não pode ser vazio
- ✅ Para DUEL: maxPlayers <= 2
- ✅ password pode ser null (público) ou string (privado)

## 🎯 Solução Recomendada para NullPointerException:

### 1. 🔧 Corrigir RoomService.java (PRINCIPAL):

```java
public RoomResponse createRoom(CreateRoomRequest request, Long hostId) {
    // ...código existente...
    
    // ✅ CORREÇÃO PRINCIPAL: Inicializar lista de players
    room.setPlayers(new ArrayList<>());
    
    Room savedRoom = roomRepository.save(room);
    return convertToRoomResponse(savedRoom);
}

private RoomResponse convertToRoomResponse(Room room) {
    // ...código existente...
    
    // ✅ PROTEÇÃO CONTRA NULL: Verificar players antes do stream
    if (room.getPlayers() != null) {
        response.setCurrentPlayers(room.getPlayers().size());
        List<PlayerResponse> players = room.getPlayers().stream()
            .map(this::convertToPlayerResponse)
            .collect(Collectors.toList());
        response.setPlayers(players);
    } else {
        response.setCurrentPlayers(0);
        response.setPlayers(new ArrayList<>());
    }
    
    return response;
}
```

### 2. 🛡️ Reforçar proteção no Room.java:

```java
@OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<Player> players = new ArrayList<>(); // ✅ Inicializar diretamente

// Getter personalizado para garantir que nunca retorne null
public List<Player> getPlayers() {
    if (this.players == null) {
        this.players = new ArrayList<>();
    }
    return this.players;
}
```

### 3. 🚀 Ordem de Implementação:

1. **Primeiro**: Corrija o `RoomService.java` (principal)
2. **Segundo**: Reforce o `Room.java` (proteção extra)  
3. **Terceiro**: Use o JSON correto no Postman
4. **Quarto**: Restart da aplicação
5. **Quinto**: Execute o teste

### 4. 📋 Resposta Esperada Após Correção:

```json
{
  "id": 1,
  "roomName": "Quiz em Equipe - Teste",
  "roomCode": "ROOM1234",
  "hostId": 1,
  "gameMode": "TEAM",
  "difficulty": "MEDIUM",
  "maxPlayers": 6,
  "currentPlayers": 0,
  "players": [],
  "status": "WAITING",
  "createdAt": "2025-08-01T21:30:00"
}
```
