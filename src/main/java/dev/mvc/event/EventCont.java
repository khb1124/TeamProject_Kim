package dev.mvc.event;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import nation.web.tool.FileVO;
import dev.mvc.notice.Notice;
import dev.mvc.notice.NoticeVO;
import dev.mvc.review.ReviewVO;
import nation.web.tool.Tool;
import nation.web.tool.Upload;

@Controller
public class EventCont {
  @Autowired
  @Qualifier("dev.mvc.event.EventProc")
  private EventProcInter eventProc;

public EventCont() {
  System.out.println("--> EventCont created");
}

   //��� �� http://localhost:9090/team4/event/create.do
  @RequestMapping(value = "/event/create.do", method = RequestMethod.GET)
  public ModelAndView create() {
    ModelAndView mav = new ModelAndView(); 
 
    mav.setViewName("/event/create"); // /webapp/event/create.jsp
    
    return mav;
  }

 // http://localhost:9090/team4/event/create.do
 @RequestMapping(value="/event/create.do", method=RequestMethod.POST)
 public ModelAndView create( HttpServletRequest request,
                                          EventVO eventVO) {
   // System.out.println("create called.");
   ModelAndView mav = new ModelAndView();
  
// -------------------------------------------------------------------
   // ���� ���� �ڵ� ����
   // -------------------------------------------------------------------
   String upDir = Tool.getRealPath(request, "/event/storage");
   // Spring�� File ��ü�� �����ص�, �����ڴ� �̿븸 ��.
   List<MultipartFile> filesMF = eventVO.getFilesMF(); 

   // System.out.println("--> filesMF.get(0).getSize(): " +
   // filesMF.get(0).getSize());

   String e_file1 = "";        // DBMS �÷��� ������ ���ϸ�
   String e_file1_item = ""; // �ϳ��� ���ϸ�
   String e_size1 = "";       // DBMS �÷��� ������ ���� ũ��
   long e_size1_item = 0;   // �ϳ��� ���� ������
   String e_thumb1 = "";    // DBMS �÷��� ������ Thumb ���ϵ�
   String e_thumb1_item = ""; // �ϳ��� Thumb ���ϸ�

   int count = filesMF.size(); // ���ε�� ���� ����

   // Spring�� ���� ������ ���ص� 1���� MultipartFile ��ü�� ������.
   // System.out.println("--> ���ε�� ���� ���� count: " + count);

   if (count > 0) { // ���� ������ �����Ѵٸ�
     // for (MultipartFile multipartFile: filesMF) {
     for (int i = 0; i < count; i++) {
       MultipartFile multipartFile = filesMF.get(i); // 0: ù��° ����, 1:�ι�° ���� ~
       // System.out.println("multipartFile.getName(): " + multipartFile.getName());

       if (multipartFile.getSize() > 0) { // ���������� �ִ��� üũ
         // System.out.println("���� ������ �ֽ��ϴ�.");
         e_file1_item = Upload.saveFileSpring(multipartFile, upDir); // ������ ���� ����
         e_size1_item = multipartFile.getSize(); // ������ ����� ���� ũ��

         if (Tool.isImage(e_file1_item)) { // �̹������� �˻�
           e_thumb1_item = Tool.preview(upDir, e_file1_item, 120, 80); // Thumb �̹��� ����
         }

         // 1�� �̻��� ���� ���� ó��
         if (i != 0 && i < count) { // index�� 1 �̻��̸�(�ι�° ���� �̻��̸�)
           // �ϳ��� �÷��� �������� ���ϸ��� �����Ͽ� ����, file1.jpg/file2.jpg/file3.jpg
           e_file1 = e_file1 + "/" + e_file1_item;
           // �ϳ��� �÷��� �������� ���� ����� �����Ͽ� ����, 12546/78956/42658
           e_size1 = e_size1 + "/" + e_size1_item;
           // �̴� �̹����� �����Ͽ� �ϳ��� �÷��� ����
           e_thumb1 = e_thumb1 + "/" + e_thumb1_item;
           
         // ������ ��� ���� ��ü�� 1�� ���������� ũ�� üũ, ������ 1���ΰ��  
         } else if (multipartFile.getSize() > 0) { 
           e_file1 = e_file1_item; // file1.jpg
           e_size1 = "" + e_size1_item; // 123456
           e_thumb1 = e_thumb1_item; // file1_t.jpg
         }
       } // if (multipartFile.getSize() > 0) {  END
     } // END for
   }
   eventVO.setE_file1(e_file1);
   eventVO.setE_size1(e_size1);
   eventVO.setE_thumb1(e_thumb1);
   // -------------------------------------------------------------------
   // ���� ���� �ڵ� ����
   // -------------------------------------------------------------------

   
   count = eventProc.create(eventVO);
   mav.setViewName("redirect:/event/create_msg.jsp?count=" + count);
   
   return mav;
 }  
  
//http://localhost:9090/ojt/notice/list.do
@RequestMapping(value="/event/list.do", method=RequestMethod.GET)
public ModelAndView list() {
  ModelAndView mav = new ModelAndView();
  mav.setViewName("/event/list");
  
  ArrayList<EventVO> list = eventProc.list();
  mav.addObject("list", list);
  
  return mav;
}  
/**
 * ��ȸ
 * 
 * @param eventno
 * @return
 */
@RequestMapping(value = "/event/read.do", method = RequestMethod.GET)
public ModelAndView read(int eventno) {
  ModelAndView mav = new ModelAndView();
  mav.setViewName("/event/read"); // /webapp/contents/read.jsp

  EventVO eventVO = eventProc.read(eventno);
  mav.addObject("eventVO", eventVO);
  
  ArrayList<FileVO> file_list = eventProc.getE_thumb1(eventVO);
  mav.addObject("file_list", file_list);

  return mav;
}
//http://localhost:9090/ojt/event/update.do?eventno=1
@RequestMapping(value="/event/update.do", 
                         method=RequestMethod.GET)
public ModelAndView update(int eventno) {
 ModelAndView mav = new ModelAndView();
 
 EventVO eventVO = eventProc.read(eventno);
 mav.addObject("eventVO", eventVO);
 
 ArrayList<FileVO>file_list = eventProc.getE_thumb1(eventVO);
 mav.addObject("file_list", file_list);
 
 mav.setViewName("/event/update"); 
 
 
 return mav;
}

// noticeVO�� Form �±��� ������ �ڵ� �����.
// http://localhost:9090/ojt/event/update.do
@RequestMapping(value="/event/update.do", 
                         method=RequestMethod.POST)
public ModelAndView update(RedirectAttributes redirectAttributes, 
                                         HttpServletRequest request, 
                                         EventVO eventVO ) {
 ModelAndView mav = new ModelAndView();

//-------------------------------------------------------------------
 // ���� ���� �ڵ� ����
 // -------------------------------------------------------------------
 String upDir = Tool.getRealPath(request, "/event/storage");
 // Spring�� File ��ü�� �����ص�, �����ڴ� �̿븸 ��.
 List<MultipartFile> filesMF = eventVO.getFilesMF(); 

 // System.out.println("--> filesMF.get(0).getSize(): " +
 // filesMF.get(0).getSize());

 String e_file1 = "";        // DBMS �÷��� ������ ���ϸ�
 String e_file1_item = ""; // �ϳ��� ���ϸ�
 String e_size1 = "";       // DBMS �÷��� ������ ���� ũ��
 long e_size1_item = 0;   // �ϳ��� ���� ������
 String e_thumb1 = "";    // DBMS �÷��� ������ Thumb ���ϵ�
 String e_thumb1_item = ""; // �ϳ��� Thumb ���ϸ�

 int count = filesMF.size(); // ���ε�� ���� ����
 
//������ ��� ���� ��ȸ
 EventVO eventVO_old = eventProc.read(eventVO.getEventno());

 if(filesMF.get(0).getSize() > 0) { // ���ο� ������ ��������� ������ ��ϵ� ���� ��� ����
   // thumbs ���� ����
   String E_thumb1_old = eventVO_old.getE_thumb1();
   StringTokenizer E_thumb1_st = new StringTokenizer(E_thumb1_old,  "/");
   while (E_thumb1_st.hasMoreTokens()) {
     String fname = upDir + E_thumb1_st.nextToken();
     Tool.deleteFile(fname); // ������ ��ϵ� thumbs ���� ����
   }
 // ���� ���� ����
 String E_file_old = eventVO_old.getE_file1();
 StringTokenizer E_file_st = new StringTokenizer(E_file_old, "/");
 while(E_file_st.hasMoreTokens()) {
   String fname = upDir + E_file_st.nextToken();
   Tool.deleteFile(fname); // ������ ��ϵ� ���� ���� ����
 }
 // --------------------------------------------
 // ���ο� ������ ��� ����
 // --------------------------------------------

 //if (count > 0) { // ���� ������ �����Ѵٸ�
   // for (MultipartFile multipartFile: filesMF) {
   for (int i = 0; i < count; i++) {
     MultipartFile multipartFile = filesMF.get(i); // 0: ù��° ����, 1:�ι�° ���� ~
      //System.out.println("multipartFile.getName(): " + multipartFile.getName());

     if (multipartFile.getSize() > 0) { // ���������� �ִ��� üũ
       // System.out.println("���� ������ �ֽ��ϴ�.");
       e_file1_item = Upload.saveFileSpring(multipartFile, upDir); // ������ ���� ����
       e_size1_item = multipartFile.getSize(); // ������ ����� ���� ũ��

       if (Tool.isImage(e_file1_item)) { // �̹������� �˻�
         e_thumb1_item = Tool.preview(upDir, e_file1_item, 120, 80); // Thumb �̹��� ����
       }

       // 1�� �̻��� ���� ���� ó��
       if (i != 0 && i < count) { // index�� 1 �̻��̸�(�ι�° ���� �̻��̸�)
         // �ϳ��� �÷��� �������� ���ϸ��� �����Ͽ� ����, file1.jpg/file2.jpg/file3.jpg
         e_file1 = e_file1 + "/" + e_file1_item;
         // �ϳ��� �÷��� �������� ���� ����� �����Ͽ� ����, 12546/78956/42658
         e_size1 = e_size1 + "/" + e_size1_item;
         // �̴� �̹����� �����Ͽ� �ϳ��� �÷��� ����
         e_thumb1 = e_thumb1 + "/" + e_thumb1_item;
         
       // ������ ��� ���� ��ü�� 1�� ���������� ũ�� üũ, ������ 1���ΰ��  
       } else if (multipartFile.getSize() > 0) { 
         e_file1 = e_file1_item; // file1.jpg
         e_size1 = "" + e_size1_item; // 123456
         e_thumb1 = e_thumb1_item; // file1_t.jpg
       }
     } // if (multipartFile.getSize() > 0) {  END
   } // END for
   // --------------------------------------------
   // ���ο� ������ ��� ����
   // --------------------------------------------
 } else { // �۸� �����ϴ� ���, ������ ���� ���� ����
   e_file1 = eventVO_old.getE_file1();
   e_size1 = eventVO_old.getE_size1();
   e_thumb1 = eventVO_old.getE_thumb1();
 }
 eventVO.setE_file1(e_file1);
 eventVO.setE_size1(e_size1);
 eventVO.setE_thumb1(e_thumb1);
 
 //eventVO.setAdminno(1);
 
 count = eventProc.update(eventVO); // DBMS ����
 // mav.setViewName("redirect:/contents/update_msg.jsp?count=" + count + "&...);
 
 redirectAttributes.addAttribute("count", count); // redirect parameter ����

 // redirect�ÿ��� request�� �����̾ȵ����� �Ʒ��� ����� �̿��Ͽ� ������ ����
 redirectAttributes.addAttribute("eventno", eventVO.getEventno());
 redirectAttributes.addAttribute("e_title", eventVO.getE_title());
 //redirectAttributes.addAttribute("nowPage", nowPage);

 mav.setViewName("redirect:/event/update_msg.jsp");
 

 return mav;
}  

// http://localhost:9090/ojt/event/delete.do?noticeno=1
@RequestMapping(value="/event/delete.do", method=RequestMethod.GET)
public ModelAndView delete(int eventno) {
  ModelAndView mav = new ModelAndView();
  mav.setViewName("/event/delete"); // /webapp/event/delete.jsp
  
  EventVO eventVO = eventProc.read(eventno);
  mav.addObject("eventVO", eventVO);
  
  return mav;
}

// cateVO�� Form �±��� ������ �ڵ� �����.
// http://localhost:9090/ojt/event/delete.do
@RequestMapping(value="/event/delete.do", method=RequestMethod.POST)
public ModelAndView delete_proc(RedirectAttributes redirectAttributes, 
                                                HttpServletRequest request, 
                                                int eventno,
                                                @RequestParam(value="word", defaultValue="") String word,
                                                @RequestParam(value="nowPage", defaultValue="1") int nowPage) {
  ModelAndView mav = new ModelAndView();
 
  String upDir = Tool.getRealPath(request, "/event/storage");
  
  // ������ ��� ���� ��ȸ
  EventVO eventVO_old = eventProc.read(eventno);
 
  String thumbs_old = eventVO_old.getE_thumb1();
  StringTokenizer thumbs_st = new StringTokenizer(thumbs_old, "/");
  while (thumbs_st.hasMoreTokens()) {
    String fname = upDir + thumbs_st.nextToken();
    Tool.deleteFile(fname); // ������ ��ϵ� thumbs ���� ����
  }
  
  // ���� ������ ����
  String files_old = eventVO_old.getE_file1();
  StringTokenizer files_st = new StringTokenizer(files_old, "/");
  while (files_st.hasMoreTokens()) {
    String fname = upDir + files_st.nextToken();
    Tool.deleteFile(fname);  // ������ ��ϵ� ���� ���� ����
  }
  int count = eventProc.delete(eventno);
  
//-------------------------------------------------------------------------------------
  // ������ �������� ���ڵ� �������� ������ ��ȣ -1 ó��
  HashMap<String, Object> map = new HashMap();
  map.put("eventno", eventno);
  map.put("word", word);
  if(eventProc.search_count(map) % Notice.RECORD_PER_PAGE == 0) {
    nowPage = nowPage - 1;
    if (nowPage < 1) {
       nowPage = 1;
    }
  }

  redirectAttributes.addAttribute("count", count);
  redirectAttributes.addAttribute("e_title", eventVO_old.getE_title());
  redirectAttributes.addAttribute("nowPage", nowPage);
  
  //mav.setViewName("redirect:/event/delete_msg.jsp?count=" + count+"&title=" + title );
  mav.setViewName("redirect:/event/delete_msg.jsp");
 
  return mav;
}  

/**
 * ��� + �˻� + ����¡ ����
 * http://localhost:9090/ojt/contents/list_by_search_paging.do
 * @param noticeno
 * @param word
 * @param nowPage
 * @return
 */
@RequestMapping(value = "/event/list_by_search_paging.do", 
                                     method = RequestMethod.GET)
public ModelAndView list_by_search_paging(
    //@RequestParam(value="noticeno", defaultValue="1") int noticeno,
    @RequestParam(value="word", defaultValue="") String word,
    @RequestParam(value="nowPage", defaultValue="1") int nowPage
    ) { 
  // System.out.println("--> list_by_category() GET called.");
  System.out.println("--> nowPage: " + nowPage);
  
  ModelAndView mav = new ModelAndView();
  
  // �˻� ��� �߰�,  /webapp/contents/list_by_category_search_paging.jsp
  mav.setViewName("/event/list_by_search_paging");   
  
  // ���ڿ� ���ڿ� Ÿ���� �����ؾ������� Obejct ���
  HashMap<String, Object> map = new HashMap<String, Object>();
 // map.put("noticeno", noticeno); // #{noticeno}
  map.put("word", word);     // #{word}
  map.put("nowPage", nowPage);       
  
  // �˻� ���
  List<EventVO> list = eventProc.list_by_search_paging(map);
  mav.addObject("list", list);
  
  // �˻��� ���ڵ� ����
  int search_count = eventProc.search_count(map);
  mav.addObject("search_count", search_count);

  //NoticeVO NoticeVO = noticeProc.read(noticeno);
  //mav.addObject("NoticeVO", NoticeVO);
  
  // mav.addObject("word", word);

  /*
   * SPAN�±׸� �̿��� �ڽ� ���� ����, 1 ���������� ���� 
   * ���� ������: 11 / 22   [����] 11 12 13 14 15 16 17 18 19 20 [����] 
   * 
   * @param listFile ��� ���ϸ� 
   * @param categoryno ī�װ�����ȣ 
   * @param search_count �˻�(��ü) ���ڵ�� 
   * @param nowPage     ���� ������
   * @param word �˻���
   * @return ����¡ ���� ���ڿ�
   */ 
  String paging = eventProc.pagingBox("list_by_search_paging.do", search_count, nowPage, word);
  mav.addObject("paging", paging);

  mav.addObject("nowPage", nowPage);
  
  return mav;
}    


}