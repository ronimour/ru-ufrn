package br.ufrn.ru_ufrn;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.sql.Date;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import br.ufrn.ru_ufrn.adapter.CardapioArrayAdapter;
import br.ufrn.ru_ufrn.adapter.MyExpandableListAdapter;
import br.ufrn.ru_ufrn.controller.CardapioController;
import br.ufrn.ru_ufrn.controller.service.ServiceResources;
import br.ufrn.ru_ufrn.model.Refeicao;
import br.ufrn.ru_ufrn.model.dao.CardapioDAO;
import br.ufrn.ru_ufrn.model.dao.CardapioSQLiteDAO;
import br.ufrn.ru_ufrn.model.dao.DAOFactory;
import br.ufrn.ru_ufrn.model.dao.GenericSQLiteDAO;
import br.ufrn.ru_ufrn.model.dao.MemoryDAOFactory;
import br.ufrn.ru_ufrn.model.dao.SQLiteDAOFactory;
import br.ufrn.ru_ufrn.services.ServiceUpdateDB;

public class Cardapio extends Activity {

	private TextView dataAtual;
	private TextView cardapioItens;
	private Button mudarData;
	private ArrayAdapter<String> adapter;
	private ListView listview;
	private SparseArray<Refeicao> groups = new SparseArray<Refeicao>();
	private final HashMap<String, br.ufrn.ru_ufrn.model.Cardapio> cardapioDaSemana = new HashMap<String, br.ufrn.ru_ufrn.model.Cardapio>();
	private CardapioController controller;
	private Intent intentServiceUpdateDB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cardapio);
		initServiceUpdateDB();
		setCurrentDateOnView();
		addListenerOnButton();
		loadCardapios();
		addListenerOnSpinner();

	}

	@Override
	protected void onStop() {
		this.stopService(intentServiceUpdateDB);
		super.onStop();

	}

	private void addListenerOnSpinner() {
		Spinner spinnerDiasSemana = (Spinner) findViewById(R.id.spinner_dias_semana);
		spinnerDiasSemana
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						String dia = (String) parent.getItemAtPosition(pos);
						br.ufrn.ru_ufrn.model.Cardapio temp = cardapioDaSemana
								.get(dia);
						createData(temp);
						setCardapioExpadableItens();
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub

					}

				});
	}

	private void initServiceUpdateDB() {
		intentServiceUpdateDB = new Intent(this, ServiceUpdateDB.class);
		this.startService(intentServiceUpdateDB);
	}

	private void loadCardapios() {

		String[] semana = { "Domingo", "Segunda", "Terça", "Quarta", "Quinta",
				"Sexta", "Sábado" };

		List<br.ufrn.ru_ufrn.model.Cardapio> cardapiosDaSemanaTemp = loadCardapiosDaSemana();
		marcarDataDoCardapio();

		if (cardapiosDaSemanaTemp == null) {
			Toast.makeText(this, "Sem cardapios da semana!", Toast.LENGTH_LONG)
					.show();
		} else {

			for (int i = 0; i < semana.length; i++) {
				if (validaCardapio(cardapiosDaSemanaTemp.get(i))) {
					cardapioDaSemana.put(semana[i],
							cardapiosDaSemanaTemp.get(i));
				} else {
					Toast.makeText(this, "Existem algum cardapio invalido!",
							Toast.LENGTH_LONG).show();
				}
			}
		}
	}

	private boolean validaCardapio(br.ufrn.ru_ufrn.model.Cardapio cardapio2) {
		boolean retorno = true;

		if (cardapio2.getCafeDaManha() == null
				|| cardapio2.getAlmocoCarnivoro() == null
				|| cardapio2.getAlmocoVegetariano() == null
				|| cardapio2.getJantaCarnivora() == null
				|| cardapio2.getJantaVegetariana() == null) {
			retorno = false;
		}

		return retorno;
	}

	private List<br.ufrn.ru_ufrn.model.Cardapio> loadCardapiosDaSemana() {
		List<br.ufrn.ru_ufrn.model.Cardapio> cardapios = null;
		if (controller == null) {
			controller = new CardapioController(this);
		}
		AsyncTask<Void, Void, List<br.ufrn.ru_ufrn.model.Cardapio>> at = new AsyncTask<Void, Void, List<br.ufrn.ru_ufrn.model.Cardapio>>() {

			@Override
			protected List<br.ufrn.ru_ufrn.model.Cardapio> doInBackground(
					Void... params) {
				return controller.getCardapiosDaSemana();
			}
		};

		at.execute();
		try {
			cardapios = at.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return cardapios;
	}

	private void marcarDataDoCardapio() {
		SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);

		SharedPreferences.Editor editor = settings.edit();
		editor.putString("data-cardapio",
				new java.sql.Date(System.currentTimeMillis()).toString());
		editor.commit();

	}

	private void createData(br.ufrn.ru_ufrn.model.Cardapio cardapio) {
		groups.append(br.ufrn.ru_ufrn.model.Cardapio.CAFE_DA_MANHA,
				cardapio.getCafeDaManha());
		groups.append(br.ufrn.ru_ufrn.model.Cardapio.ALMOCO_VEGETARIANO,
				cardapio.getAlmocoVegetariano());
		groups.append(br.ufrn.ru_ufrn.model.Cardapio.ALMOCO_CARNIVORO,
				cardapio.getAlmocoCarnivoro());
		groups.append(br.ufrn.ru_ufrn.model.Cardapio.JANTA_VEGETARIANA,
				cardapio.getJantaVegetariana());
		groups.append(br.ufrn.ru_ufrn.model.Cardapio.JANTA_CARNIVORA,
				cardapio.getJantaCarnivora());

	}

	private void setCardapioExpadableItens() {
		ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
		MyExpandableListAdapter adapter = new MyExpandableListAdapter(this,
				groups);
		listView.setAdapter(adapter);
	}

	private void setCardapioItens(br.ufrn.ru_ufrn.model.Cardapio cardapio) {
		br.ufrn.ru_ufrn.model.Cardapio temp = cardapio;
		cardapioItens = (TextView) findViewById(R.id.textView_cardapio_itens);
		cardapioItens.setText(temp.toString());

		String[] refeicoes = new String[] { "Café da manhã",
				"Almoço Vegetariano", "Almoço Carnívoro", "Janta Vegetariana",
				"Janta Carnívora" };
		adapter = new CardapioArrayAdapter(this, R.layout.rowlayout, refeicoes);
		// TODO quebra!
		listview = (ListView) findViewById(R.id.listView);
		listview.setAdapter(adapter);

	}

	private void addListenerOnButton() {

		mudarData = (Button) findViewById(R.id.button_mudar_data);
		mudarData.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				DatePickerFragment newFragment = new DatePickerFragment();
				newFragment
						.setData((TextView) findViewById(R.id.textView_data_atual));
				newFragment.show(getFragmentManager(), "datePicker");

			}
		});

	}

	public void alterarData(Calendar c) {
		dataAtual = (TextView) findViewById(R.id.textView_data_atual);
		dataAtual.setText(c.getTime().toString());
	}

	private void setCurrentDateOnView() {

		final Calendar c = Calendar.getInstance();

		dataAtual = (TextView) findViewById(R.id.textView_data_atual);
		dataAtual
				.setText(new SimpleDateFormat("yyyy-MM-dd").format(c.getTime()));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.cardapio, menu);
		return true;

	}

}
