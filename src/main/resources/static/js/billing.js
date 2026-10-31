(function () {
  const form = document.getElementById('billForm');
  if (!form) return;

  const shopState = (form.dataset.shopState || '').trim().toLowerCase();
  const tbody = document.getElementById('lines');
  const tpl = document.getElementById('rowTemplate');
  const customerSel = document.getElementById('customerId');
  const paidInput = document.getElementById('amountPaid');
  let paidEdited = false;

  const round2 = n => Math.round((n + Number.EPSILON) * 100) / 100;
  const fmt = n => '\u20B9' + n.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  const set = (id, text) => { document.getElementById(id).textContent = text; };

  function isInterState() {
    const opt = customerSel.options[customerSel.selectedIndex];
    const st = ((opt && opt.dataset.state) || '').trim().toLowerCase();
    return st !== '' && st !== shopState;
  }

  function recalc() {
    let subtotal = 0, tax = 0;
    tbody.querySelectorAll('tr').forEach(tr => {
      const sel = tr.querySelector('select');
      const qtyInput = tr.querySelector('input[name=quantity]');
      const opt = sel.options[sel.selectedIndex];
      if (!opt || !opt.value) {
        tr.querySelector('.rate').textContent = '-';
        tr.querySelector('.gst').textContent = '-';
        tr.querySelector('.amt').textContent = '-';
        return;
      }
      const price = parseFloat(opt.dataset.price);
      const gst = parseFloat(opt.dataset.gst);
      const stock = parseInt(opt.dataset.stock, 10);
      qtyInput.max = stock;
      const qty = Math.max(parseInt(qtyInput.value, 10) || 0, 0);
      const taxable = round2(price * qty);
      const t = round2(taxable * gst / 100);
      subtotal += taxable;
      tax += t;
      tr.querySelector('.rate').textContent = fmt(price);
      tr.querySelector('.gst').textContent = gst + '%';
      tr.querySelector('.amt').textContent = fmt(taxable + t);
    });

    const inter = isInterState();
    const cgst = inter ? 0 : round2(tax / 2);
    const sgst = inter ? 0 : round2(tax - cgst);
    const igst = inter ? tax : 0;
    const raw = subtotal + tax;
    const grand = Math.round(raw);

    set('tSubtotal', fmt(subtotal));
    document.getElementById('rowCgst').style.display = inter ? 'none' : 'flex';
    document.getElementById('rowSgst').style.display = inter ? 'none' : 'flex';
    document.getElementById('rowIgst').style.display = inter ? 'flex' : 'none';
    set('tCgst', fmt(cgst));
    set('tSgst', fmt(sgst));
    set('tIgst', fmt(igst));
    set('tRound', fmt(grand - raw));
    set('tGrand', fmt(grand));
    if (!paidEdited) paidInput.value = grand.toFixed(2);
  }

  function addRow() {
    tbody.appendChild(tpl.content.cloneNode(true));
    recalc();
  }

  document.getElementById('addRow').addEventListener('click', addRow);
  tbody.addEventListener('input', recalc);
  tbody.addEventListener('change', recalc);
  tbody.addEventListener('click', e => {
    if (e.target.classList.contains('btn-remove')) {
      e.target.closest('tr').remove();
      if (!tbody.children.length) addRow(); else recalc();
    }
  });
  customerSel.addEventListener('change', recalc);
  paidInput.addEventListener('input', () => { paidEdited = true; });

  addRow();
})();
