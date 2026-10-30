# Аудио

Включены записи, добавленные владельцем репозитория в `main` (464935d).
Исходные MP3 сохранены без изменений; игровые ресурсы перекодированы в OGG Vorbis
(качество 5) с помощью FFmpeg. Временные синтезированные эффекты заменены.
`musor-drop_P5245U6.mp3` используется для пролёта жителя за каждый заработанный стак.

Для замены добавьте записи, которые вы вправе использовать, в
`src/main/resources/assets/pepe/sounds/`:

| Файл (OGG Vorbis, не переименованный MP3) | Запись |
| --- | --- |
| music.ogg | Villager Sings MONTAGEM XONADA (AI Cover) |
| cash.ogg | cash-register-sound-fx |
| error.ogg | wrong-answer-buzzer |
| off.ogg | perfect-fart |
| musor_drop.ogg | musor-drop_P5245U6 |
| combo_1.ogg … combo_8.ogg | soundboard combo 1 … combo 8 |

Затем пересоберите мод. Или замените их ресурс-паком по тем же путям
`assets/pepe/sounds/…` (включая `music.ogg`); события звука уже объявлены в sounds.json.
Музыка зацикливается в категории «Музыка», остальные эффекты — «Игроки».
После смены ресурс-пака выключите/включите режим C.

## 1.2.0

`slot-machine-jackpot-sound-effect.mp3` from origin/main replaces `musor_drop` as
`assets/pepe/sounds/slotmachinejackpot.ogg` (Vorbis q5). Original recording 7.131 s.
Stack animation/sound start together; another jackpot waits at least 144 ticks
and until playback ends. Music ducks smoothly during jackpot/combo playback.
Combo timings match source recordings, finale gets its own 115-tick visual window.
