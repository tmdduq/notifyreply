# notifyreply

>카카오톡 자동응답봇.. 



스토어에 엄청많은데 나도 한 번..







# 사용법

> 봇에게 학습시키기 

- 명령어> 학습하기+키워드+응답메시지 

- 예시) 학습하기+나는+배가고프다 

  ※ 학습 메시지는 채팅방마다 따로 운영 
 
 
 
 
 
> 학습내용 확인 

- 명령어> 학습목록보기, ㅎㅅㅁㄹㅂㄱ, ㅎㅅㅁㄼㄱ 

- 명령어> 학습목록보기 전체다   (※모든방의 학습내용 조회) 




> 학습내용 삭제 

- 명령어> 학습내용삭제 [대상번호] 

- 예시) 학습내용삭제 3 




> API 메시지 

>4-1)날씨 

- 명령어> [시군구] [동] 날씨 

- 예시) 연수구 송도1동 날씨 




>4-2)코로나  

- 명령어> 코로나 현황 




>4-3)부동산 거래가격 

- 명령어> [광역시도] [시군구] 실거래가 

- 예시) 서울특별시 동작구 실거래가 

- 예시) 경기도 성남시 분당구 실거래가 




>4-4)코인가격 

- 명령어> [코인명] 시세 

- 예시) 비트코인 시세 

- 예시) BTC 시세 

- 예시) 모든코인 시세 




>4-5)국어사전검색 

- 명령어> [검색어]가 뭐야 

- 명령어> [검색어]이 뭐야 

- 예시) 나무가 뭐야 




>5. 온/오프 

- 명령어> 이제그만 

- 명령어> 다시작동 

- 앱에서 [On/Off]버튼 클릭 (모든방에서 중지) 


# 개발자를 위한 서비스 플로우
- 어플 실행
 1. MainActivity.java -> onCreate() 실행. 간단한 화면 하나를 만듦
 2. NotifiService.java -> 알림바(Notification) 서비스 리스너 실행.
- 카톡 메시지 수신
 3. NotifiService.java -> onNotificationPosted() 실행.
 4. sendReply() 받은 메시지를 파싱 -> 또 언제쓸지 모르니 MainActivity로 브로트캐스트
- 답장 문자열 생성
 5. ReplyConstraint.java ->  checkKeyword()로 메시지를 체크하여 답장 문자열 생성
 6. NotifiService -> sendReply()로 답장 전송 


# 
> 알림바에서 파싱해올 수 있는 21가지 메시지 (sample)
> 
 >>a. android.title : 보낸사람이름
 >>
 >>b. android.reduced.images : true
 >>
 >>c. android.subText : 채팅방이름
 >>
 >>d. android.template : android.app.Notification$BigTextStyle
 >>
 >>e. android.showChronometer : false
 >>
 >>f. mIndex : 2,3,5,8,9
 >>
 >>g. android.text : 받은메시지
 >>
 >>h. android.progress : 0
 >>
 >>i.androidx.core.app.extra.COMPAT_TEMPLATE : androidx.core.app.NotificationCompat$BigTextStyle
 >>
 >>j. android.progressMax : 0
 >>
 >>k. android.appInfo : ApplicationInfo{e588cb8 com.kakao.talk}\n
 >>
 >>l. android.showWhen : true
 >>
 >>m. android.largeIcon : Icon(typ=BITMAP size=95x95)
 >>
 >>n. android.bigText : 받은메시지
 >>
 >>o. android.infoText : null
 >>
 >>p. android.wearable.EXTENSIONS : Bundle[{actions=[android.app.Notification$Action@4e42a91], pages=[Notification(channel=null shortcut=null contentView=null vibrate=null sound=null defaults=0x0 flags=0x0 color=0x00000000 vis=PRIVATE semFlags=0x0 semPriority=0 semMissedCount=0)]}]
 >>
 >>q. android.progressIndeterminate : false
 >>
 >>r. android.remoteInputHistory : null
 >>
 >>s. android.summaryText : 채팅방이름
 >>
 >>t. android.title.big : 보낸사람이름
 >>
 >>u. chatLogId : 2624473650577811456
 >>


