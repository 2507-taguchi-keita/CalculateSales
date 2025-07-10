package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {

		//コマンドライン引数が渡されているか確認するエラーを表示
		if (args.length != 1) {
			System.out.println(UNKNOWN_ERROR);
			return;
		}

		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if (!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		File[] files = new File(args[0]).listFiles();

		//売上ファイルだけが入っている(まだfile型)
		List<File> rcdFiles = new ArrayList<>();

		for (int i = 0; i < files.length; i++) {
			//ファイルの名前を取得する処理
			String fileName = files[i].getName();

			//売上ファイルを判定する処理
			if (fileName.matches("^[0-9]{8}[.]rcd$")) {
				rcdFiles.add(files[i]);
			}
		}

		//売上ファイルのファイル名が連番になっていないエラーを表示
		for (int i = 0; i < rcdFiles.size() - 1; i++) {

			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i + 1).getName().substring(0, 8));

			if ((latter - former) != 1) {
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}

		//売上ファイルのファイル数分繰り返す
		for (int i = 0; i < rcdFiles.size(); i++) {

			//売上ファイル読み込み処理
			BufferedReader br = null;

			try {
				//売上ファイルを開く
				File file = new File(args[0], rcdFiles.get(i).getName());
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);

				//売上ファイルの中を保持するためのリストを作成(string型)
				List<String> saleFile = new ArrayList<>();
				String line;

				//readLine…売上ファイルの中身を１行ずつ読み込む　読んだものはString
				while ((line = br.readLine()) != null) {
					//売上ファイルの1行目(whileの0周目のline)には支店コード、2行目(whileの1周目のline)には売上金額が入る
					saleFile.add(line);
				}

				//売上ファイルのフォーマットが異なるというエラーを表示
				if (saleFile.size() != 2) {
					System.out.println(rcdFiles.get(i).getName() + "のフォーマットが不正です");
					return;
				}

				//⽀店に該当がなかった場合は、エラーメッセージ「<該当ファイル名>の⽀店コードが不正です」と表示
				if (!branchNames.containsKey(saleFile.get(0))) {
					System.out.println(rcdFiles.get(i).getName() + "の支店コードが不正です");
					return;
				}

				//読み込んだ売上金額をMapに加算するために型の変換を行う。
				long fileSale = Long.parseLong(saleFile.get(1));

				//読み込んだ売上金額を加算
				Long saleAmount = branchSales.get(saleFile.get(0)) + fileSale;

				//合計⾦額が10桁を超えた場合、エラーメッセージ「合計金額が10桁を超えました」を表示
				if (saleAmount >= 10000000000L) {
					System.out.println("合計金額が10桁を超えました");
					return;
				}

				//加算した売上金額をMapに追加
				branchSales.put(saleFile.get(0), saleAmount);

			} catch (IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}
		}

		// 支店別集計ファイル書き込み処理
		if (!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

	} //mainメソッド

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);

			//支店定義ファイルが読み込めませんのエラーを表示
			if (!file.exists()) {
				System.out.println(FILE_NOT_EXIST);
				return false;
			}

			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読込
			while ((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				String[] items = line.split(",");

				//支店定義ファイルのフォーマットが不正ですのエラーを表示
				if ((items.length != 2) || (!items[0].matches("^[0-9]{3}$"))) {
					System.out.println(FILE_INVALID_FORMAT);
					return false;
				}

				branchNames.put(items[0], items[1]);
				branchSales.put(items[0], 0L);
			}

		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		BufferedWriter bw = null;

		try {
			File file = new File(path, fileName);
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);

			//MapからKeyの⼀覧を取得してKeyの数だけ書き込みを繰り返す
			for (String key : branchNames.keySet()) {
				bw.write(key + "," + branchNames.get(key) + "," + branchSales.get(key));
				bw.newLine();

			}
		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;

				}
			}
		}
		return true;
	}
}
