# notifyreply

>카카오톡 자동응답봇.. 



스토어에 엄청많은데 나도 한 번..







# 사용법

                seq+++">코로나 현황 확인\n" +
                        
                        
                        "ex) 코로나",
                
                
                seq+++">국어사전 검색\n" +
                        
                        
                        "-> [검색어]가 뭐야" +
                        
                        
                        "ex) 나무가 뭐야",
                
                
                seq+++">날씨 확인\n" +
                        
                        
                        "-> 오늘 날씨\n" +
                        
                        
                        "-> [구] [동] 날씨\n" +
                        
                        
                        "ex) 연수구 송도1동 날씨",
                
                
                seq+++">인기 검색어 확인\n" +
                        
                        
                        "ex) 실시간 검색어\n" +
                        
                        
                        "ex) 인기 검색어",
                
                
                seq+++">타임스페이스CGV 확인\n" +
                        
                        
                        "ex) CGV\n" +
                        
                        
                        "ex) 내일 CGV\n" +
                        
                        
                        "ex) 금요일 CGV\n" +
                        
                        
                        "-> CGV [제목] 시간표\n" +
                        
                        
                        "ex) CGV 인셉션 시간표\n" +
                        
                        
                        "ex) CGV 토요일 인셉션 시간표\n" +
                        
                        
                        "ex) CGV 내일 인셉션 시간표",
                
                
                seq+++">실시간 코인 가격\n" +
                        
                        
                        "-> [코인명] 시세\n" +
                        
                        
                        "-> [코인약어] 시세\n" +
                        
                        
                        "ex) 이더리움 시세\n" +
                        
                        
                        "ex) ETH 시세\n" +
                        
                        
                        "ex) 모든코인 시세",
                
                
                seq+++">뉴스 확인\n" +
                        
                        
                        "ex) 데일리뉴스 보기\n" +
                        
                        
                        "ex) 데일리뉴스 구독\n" +
                        
                        
                        "ex) 데일리뉴스 구독 취소",
                
                
                seq+++">롤 초성퀴즈 풀기\n" +
                        
                        
                        "ex) 퀴즈 시작\n" +
                        
                        
                        "ex) 퀴즈 중지\n" +
                        
                        
                        "ex) 힌트",
                
                
                seq+++">부동산 실거래가 확인\n" +
                        
                        
                        "-> [시] [구] 실거래가\n" +
                        
                        
                        "ex) 인천광역시 연수구 실거래가",
                
                
                seq+++">청약 정보 확인\n" +
                        
                        
                        "ex) 청약정보\n" +
                        
                        
                        "ex) 청약정보 특별공급\n" +
                        
                        
                        "ex) 청약정보 2순위",
                
                
                seq+++">로또번호 추천\n" +
                        
                        
                        "ex) 로또 추천",
                
                
                seq+++">말 가르치기\n" +
                        
                        
                        "-> 학습하기+[키워드]+[대답]\n" +
                        
                        
                        "ex) 학습하기+배고파+밥먹어라",
                
                
                seq+++">학습한 내용보기/삭제\n" +
                        
                        
                        "-> 학습목록보기\n" +
                        
                        
                        "-> 학습목록삭제 39",
                
                
                seq+++">봇 멈추기/재시작\n" +
                        
                        
                        "-> 이제 그만\n" +
                        
                        
                        "-> 다시 작동"







# 개발자를 위한 서비스 플로우
- 어플 실행
 1. MainActivity.java -> onCreate() 간단한 화면 하나를 만듦
 2. ReplyConstraint.java -> setInitialize() 앱에 내장된 DB에 저장된 지난 학습내용을 메모리(static roomNodes변수)로 가져옴
- Key:Value가 1:N인 3단계 트리구조의 자료형이 뭐가 있는지 몰라서 임의의 구조체(DataRoom＜Generic T＞ class)를 만들어서 사용
- Map도 생각해봤는데 Map은 하나의 키워드(Key)에 중복 응답메시지(Value) 입력이 안됨.. // 배열은 사이즈 확장이 안됨... // 링크드리스트랑 벡터까지 생각해봤는데 ArrayList가 속도가 가장 빠르다고해서 이걸 기반으로 해봄
 4. NotifiService.java -> 알림바(Notification) 서비스 리스너 실행. (백그라운드, 포어그라운드에서 동작)
- 카톡 메시지 수신
 4. 카톡이 오는 것을 Listen.... ............. ...... ...
 5. 왔다!! NotifiService.java -> onNotificationPosted() 실행.
 6. sendReply() 받은 알림(Notification)을 파싱 -> 또 언제쓸지 모르니 MainActivity로 브로트캐스트
- 답장 문자열 생성
 7. ReplyConstraint.java ->  checkKeyword()로 메시지를 체크하여 답장 문자열 생성
 8. NotifiService -> sendReply()로 답장 전송 


# 
> 알림바에서 파싱해올 수 있는 21가지 메시지 (sample)
> 
> 스토어에 많고 많은 카톳봇이 있지만 response함수가 다 똑같이 생겼던 이유는 써먹을 만한게 그것밖에 없었기 때문.. 
 >>a. android.title : 보낸사람이름 ★★
 >>
 >>b. android.reduced.images : true
 >>
 >>c. android.subText : 채팅방이름 ★★ (null이면 갠톡)
 >>
 >>d. android.template : android.app.Notification$BigTextStyle
 >>
 >>e. android.showChronometer : false
 >>
 >>f. mIndex : 2,3,5,8,9
 >>
 >>g. android.text : 받은메시지 ★★
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

