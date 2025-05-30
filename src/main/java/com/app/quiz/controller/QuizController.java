package com.app.quiz.controller;

// 必要なクラスのインポート
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;

import com.app.quiz.service.QuizUserDetailsService;
import com.app.quiz.model.Quiz;
import com.app.quiz.service.QuestionsService;

/*
*mvn clean (maven clean) を使用して既存のクラスファイルをクリーンし、
*mvn install (maven install) を使用してプロジェクトディレクトリ内のファイルをコンパイルし、実行可能な jar ファイルを生成します。
*
* 「　mvn clean install　」を実行
*
*
*次のコマンドを実行してアプリケーションを起動します。ポート 8080 で起動します。
*mvn exec:java -Dexec.mainClass="com.app.quiz.QuizApplication"
*/


@Controller // コントローラーであることを示すアノテーション
public class QuizController {

	private final QuizUserDetailsService userDetailsService; // ユーザー登録・取得サービス
	private final QuestionsService questionsService; // クイズ管理サービス
	private final AuthenticationManager authenticationManager; // Spring Security 認証マネージャ

    // コンストラクタインジェクションでサービスを受け取る
    public QuizController(QuizUserDetailsService userDetailsService, AuthenticationManager authenticationManager, QuestionsService questionsService) {
        this.userDetailsService = userDetailsService;
		this.authenticationManager = authenticationManager;
		this.questionsService = questionsService;
    }
    
	@GetMapping("/home") // ホーム画面にアクセスしたときの処理
	public String homepage(Model model) {
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // 現在の認証情報を取得
	    String username = authentication.getName(); // ユーザー名を取得
	    model.addAttribute("username", username); // ビューにユーザー名を渡す

	    String role = authentication.getAuthorities().stream() // 権限リストからロールを取得
	        .map(GrantedAuthority::getAuthority)
	        .findFirst()
	        .orElse("ROLE_USER"); // 権限がない場合のデフォルト

	    // ロールによって表示ページを分ける
	    if (role.equals("ROLE_ADMIN")) {
			List<Quiz> quizzes = questionsService.getQuizzesList(); // 全クイズを取得
			model.addAttribute("quizzes", quizzes); // モデルに追加
	        return "QuizList"; // 管理者用クイズリストページ
	    } else {
			List<Quiz> quizzes = questionsService.getQuizzesList(); // 全クイズを取得
			model.addAttribute("quizzes", quizzes); // モデルに追加
	        return "Quiz"; // 一般ユーザー用クイズページ
	    }
	}

	@GetMapping("/login") // ログインページ表示
    public String login() {
        return "login"; // login.html を返す
    }

	@GetMapping("/register") // 登録ページ表示
    public String register() {
        return "register"; // register.html を返す
    }

	// 登録フォームからのPOSTリクエストを処理
	@PostMapping("/register")
	public String registerUser(
			@RequestParam String username, // フォームからのユーザー名
			@RequestParam String email, // メールアドレス
			@RequestParam String password, // パスワード
			@RequestParam String role // ロール
	) {
		try {
			userDetailsService.registerUser(username, password, email, role); // ユーザーを登録
		} catch (Exception userExistsAlready) {
			return "redirect:/register?error"; // 登録済みの場合はエラー
		}

		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(username, password) // 自動ログイン
		);

		SecurityContextHolder.getContext().setAuthentication(authentication); // 認証状態を設定
		return "redirect:/login?success"; // ログインページへリダイレクト
	}	

	@GetMapping("/addQuiz") // クイズ追加フォームの表示
    public String showAddQuizForm(Model model) {
        model.addAttribute("quiz", new Quiz()); // 空のQuizオブジェクトをモデルに追加
        return "addQuiz"; // addQuiz.html を返す
    }

	@PostMapping("/addQuiz") // クイズ追加フォームの送信処理
	public String addQuiz(@ModelAttribute Quiz quiz, Model model, Authentication authentication) {
	    String role = authentication.getAuthorities().stream()
	        .map(GrantedAuthority::getAuthority)
	        .findFirst()
	        .orElse("ROLE_USER"); // ロールを取得

	    if (role.equals("ROLE_ADMIN")) {
			quiz.setId(questionsService.getNextId()); // 一意なIDを設定
	        questionsService.addQuiz(quiz); // クイズを追加
	        model.addAttribute("success", "Quiz added successfully!"); // 成功メッセージ
	        return "redirect:/home"; // ホームへリダイレクト
	    } else {
	        model.addAttribute("error", "You do not have permission to add a quiz."); // エラーメッセージ
	        return "redirect:/addQuiz?error"; // フォームへ戻す
	    }
	}

	@GetMapping("/editQuiz/{id}") // クイズ編集ページの表示
    public String showEditQuizForm(@PathVariable("id") int id, Model model) {
        Quiz quiz = questionsService.getQuizById(id); // IDでクイズ取得
        model.addAttribute("quiz", quiz); // モデルに追加
        return "editQuiz"; // editQuiz.html を返す
    }

	@PostMapping("/editQuestion") // クイズ編集フォームの送信処理
	public String editQuestion(@ModelAttribute("quiz") Quiz quiz) {
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // 認証情報取得
	    String role = authentication.getAuthorities().stream()
	        .map(GrantedAuthority::getAuthority)
	        .findFirst()
	        .orElse("ROLE_USER"); // ロール取得

	    if (role.equals("ROLE_ADMIN")) {
	        questionsService.editQuiz(quiz); // クイズを更新
	        return "redirect:/home"; // ホームへ
	    } else {
	        return "redirect:/home"; // 権限がなければそのままホームへ
	    }
	}

	@GetMapping("/deleteQuiz/{id}") // クイズ削除処理
	public String deleteQuiz(@PathVariable("id") int id, Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // 認証情報取得
		String role = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.findFirst()
			.orElse("ROLE_USER"); // ロール取得

		if (role.equals("ROLE_ADMIN")) {
			questionsService.deleteQuiz(id); // クイズ削除
			return "redirect:/home"; // ホームへ
		} else {
			return "redirect:/home"; // 権限がなければそのままホームへ
		}
	}

	@PostMapping("/submitQuiz") // クイズ回答の送信処理
	public String evaluateQuiz(@RequestParam Map<String, String> allParams, Model model) {
	    int correctAnswers = 0; // 正解数カウント
	    List<String> userAnswers = new ArrayList<>(); // ユーザーの回答リスト
	    ArrayList<Quiz> quizzes = questionsService.getQuizzesList(); // クイズ一覧取得

	    // 各クイズに対して回答を検証
	    for (int i = 0; i < quizzes.size(); i++) {
	        String userAnswer = allParams.get("answer" + i); // ユーザーの回答取得
	        userAnswers.add(userAnswer); // 回答をリストに追加
	        if (quizzes.get(i).getCorrectAnswer().equals(userAnswer)) {
	            correctAnswers++; // 正解ならカウント
	        }
	    }

	    // 結果をモデルに追加
	    model.addAttribute("quizzes", quizzes);
	    model.addAttribute("userAnswers", userAnswers);
	    model.addAttribute("correctAnswers", correctAnswers);
	    model.addAttribute("totalQuestions", quizzes.size());

	    return "result"; // 結果ページ（result.html）を表示
	}
}
